/*
 * 文件名：SlaveListenerThread.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： SlaveListenerThread.java
 * 修改人：刘红艳
 * 修改时间：2016年8月12日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youanmi.sky.core.transaction.bizdata.service.IMasterBizdataService;
import com.youanmi.sky.core.transaction.conf.ZKConf;
import com.youanmi.sky.core.transaction.constant.ZKTransactionConst;
import com.youanmi.sky.core.transaction.dto.ListenerSwitchDto;
import com.youanmi.sky.core.transaction.dto.MasterNodeDto;
import com.youanmi.sky.core.transaction.dto.TransactionCountDto;
import com.youanmi.sky.core.transaction.utils.ZookeeperUtils;


/**
 * 监听被控方事务完成状况。
 * <p>
 * 1.当被控方完成事务，则主控方累计子服务完成事务数+1，全部完成则删除控制节点。和记录主控方事务完成状态。
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class SlaveListenerThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(SlaveListenerThread.class);

    private MasterNodeDto master;

    private CuratorFramework client;

    private IMasterBizdataService masterBizdataService;
    /** 哪个服务，用于记录 */
    private String server;


    public SlaveListenerThread(MasterNodeDto master, IMasterBizdataService masterBizdataService,
            String server) {
        super();
        this.master = master;
        this.masterBizdataService = masterBizdataService;
        this.server = server;
    }


    private void listener(ListenerSwitchDto listener) throws Exception {
        String path =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR + ZKTransactionConst.Node.NODE_COUNT;
        LOG.info("监听:" + path);
        try {
            client = ZookeeperUtils.createClient();
            client.start();
            /**
             * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
             */
            ExecutorService pool = Executors.newFixedThreadPool(1);

            /**
             * 监听数据节点的变化情况
             */
            final NodeCache nodeCache = new NodeCache(client, path, false);
            nodeCache.start(true);
            nodeCache.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    byte[] b=null;
                    if(nodeCache.getCurrentData()!=null){
                        b=nodeCache.getCurrentData().getData();
                    }
                    if(b!=null){
                        String data = new String(nodeCache.getCurrentData().getData());
                        process(data);
                    }
                }


                private void process(String data) {
                    LOG.info("Node data is changed, new data: " + data);
                    TransactionCountDto txc = new TransactionCountDto(data);
                    if (txc.getCount() != null) {
                        Integer c = txc.getCount();
                        Integer txC = txc.getTxCount();
                        if (c.intValue() == txC.intValue()) {
                            listener.setListener(false);
                            listener.setSuccess(true);
                            LOG.info("监听到事务完成准备删除节点");
                        }
                        else if(txc.getFailedCount()>0){
                            listener.setListener(false);
                            listener.setSuccess(false);
                            LOG.info("监听到子事务异常，停止被控方监听，数据进入审核列表");
                        }
                    }
                }
            }, pool);
            Long start = System.currentTimeMillis();
            int i = 1;
            while (listener.isListener()) {
                if(i>6){// 监听了6分钟后停止监听，要求强事务处理在一定时间内必须完成，避免特殊异常下占用资源
                    LOG.info("监听时长过长，停止监听:"+path+":" + i);
                    break;
                }
                Long now = System.currentTimeMillis();
                if (now - start > (1000 * 60) * i) {
                    if (client.checkExists().forPath(path) == null) {// requestId节点被删除了也停止监听
                        LOG.info(path + " 已不存在，停止监听。");
                        break;
                    }
                    LOG.info("正在监听:"+path+":" + (i++));// 一定要有执行逻辑代码，否则不走到这里？
                }
                Thread.sleep(20);
            }
            LOG.info("监听完成，事务处理完成");
            // 这个3个步骤需要放在这里，有顺序执行，不然会无法删除节点
            pool.shutdown();// 1：停止监听,(停了监听才能删)
            if (!listener.isListener()&&listener.isSuccess()) {// 主动通知停止监听，代表由监听程序监听到的，要继续处理逻辑。否则是监控到节点不存在才停止监听，不需要处理业务逻辑。
                deleteRequestIdNode();// 2：删除请求节点及解锁对象
                // 3.修改主控方完成事务
                masterBizdataService.success(master.getRequestId(), server);
            }
        }
        finally {
            client.close();// 3:关闭连接
        }
    }


    /**
     * 删除请求节点
     */
    private void deleteRequestIdNode() {
        String objPath =ZKConf.getRootPath() + master.getObjId();
        String deletePath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId();
        try {
            if (master.getIsLock()) {// 1.锁了资源则解锁
                CuratorTransaction ct = client.inTransaction();// 开启事务
                ct.setData().forPath(objPath, Boolean.FALSE.toString().getBytes()).and().commit();
            }
            else {
                // do nothing
            }
            // 2.删了请求节点(子节点全部删除无法在事务中执行？)
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(deletePath);
            if (client.checkExists().forPath(deletePath) != null) {
                client.delete().forPath(deletePath);
            }
            LOG.info("监听到事务完成删除节点成功。"+deletePath);
        }
        catch (Exception e) {
            LOG.error("整体事务成功，删除请求节点异常："+deletePath,e);
        }
    }


    @Override
    public void run() {
        try {
            this.listener(new ListenerSwitchDto(true));
        }
        catch (Exception e) {
            // Auto-generated catch block
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to run", e);
            }
            e.printStackTrace();
        }
    }
}
