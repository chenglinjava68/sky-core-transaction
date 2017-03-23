/*
 * 文件名：MasterListenerThread.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： MasterListenerThread.java
 * 修改人：刘红艳
 * 修改时间：2016年8月13日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youanmi.sky.core.transaction.bizdata.service.ISlaveBizdataService;
import com.youanmi.sky.core.transaction.conf.ZKConf;
import com.youanmi.sky.core.transaction.constant.ZKTransactionConst;
import com.youanmi.sky.core.transaction.dto.ListenerSwitchDto;
import com.youanmi.sky.core.transaction.dto.MasterNodeDto;
import com.youanmi.sky.core.transaction.dto.TransactionCountDto;
import com.youanmi.sky.core.transaction.utils.ZookeeperUtils;


/**
 * 监听主控方事务完成状态。
 * <p>
 * 1.当主控方完成事务，则所有的被控方都执行数据生效逻辑。
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class MasterListenerThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(MasterListenerThread.class);

    private MasterNodeDto master;

    private CuratorFramework client;

    private ISlaveBizdataService slaveBizdataService;

    /**
     * 哪个服务节点在监听
     */
    private String server;


    public MasterListenerThread(MasterNodeDto master, ISlaveBizdataService slaveBizdataService, String server) {
        this.master = master;
        this.slaveBizdataService = slaveBizdataService;
        this.server = server;
    }


    private void listener(ListenerSwitchDto listener) throws Exception {
        String path =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId();
        LOG.info("监听:" + path);
        try {
            client = ZookeeperUtils.createClient();
            client.start();
            byte[] bd = client.getData().forPath(path);
            String d = (bd == null) ? null : new String(bd);
            if(ZKTransactionConst.Status.COMPLETE.equals(d)){//一监听即判断主控方是否完成事务
                confirm(listener);
            }else{//未完成进入线程监控
                
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
                        byte[] b = null;
                        if (nodeCache.getCurrentData() != null) {
                            b = nodeCache.getCurrentData().getData();
                        }
                        if (b != null) {
                            String data = new String(nodeCache.getCurrentData().getData());
                            process(data);
                        }
                    }
        
                    /**
                     * 处理节点数据
                     * 
                     * @throws Exception
                     */
                    private void process(String data) throws Exception {
                        LOG.info("Node data is changed, new data: " + data);
                        if (data == null || data.trim().equals("")) {
                            LOG.info("Count data is null");
                        }
                        else if (data.equals(ZKTransactionConst.Status.COMPLETE)) {// 监听到requestId节点完成请求
                            confirm(listener);
                        }
                        else {
                            LOG.info("Count data is unnormal." + data);
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
                    if (System.currentTimeMillis() - start > (1000 * 60) * i) {// 每隔一分钟检测下节点存在没
                        if (client.checkExists().forPath(path) == null) {// requestId节点被删除了也停止监听
                            LOG.info(path + "已不存在，停止监听。");
                            break;
                        }
                        LOG.info("正在监听:" + path + ":" + (i++));// 一定要有执行逻辑代码，否则不走到这里？
                    }
                    Thread.sleep(20);
                }
                LOG.info("监听完成，事务处理完成");
                pool.shutdown();// 1：停止监听
            }
        }
        finally {
            client.close();// 3:关闭连接
        }
    }


    /**
     * 事务确认，数据生效。
     * 
     * @param listener
     * @throws Exception
     */
    private void confirm(ListenerSwitchDto listener) throws Exception {
        try {
            // 1.数据生效 need api
            slaveBizdataService.confirm(master.getRequestId(), server);
            // 2.count 节点完成事务数+1
            incrementTxCount();
        }
        catch (Exception e) {
            LOG.error("执行数据生效失败。", e);
            // 数据生效失败则记录节点状态为失败，该部分进入数据审核。继续重试。
            faildRecord();
        }
        // 3.停止监听
        listener.setListener(false);
    }


    /**
     * 完成事务总数+1,状态变为C。
     * 
     * @throws Exception
     */
    private void incrementTxCount() throws Exception {
        String countPath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + ZKTransactionConst.Node.NODE_COUNT;

        String serverPath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR + server;
        byte[] b = client.getData().forPath(countPath);
        if (b != null) {
            String countData = new String(client.getData().forPath(countPath));
            TransactionCountDto txc = new TransactionCountDto(countData);
            if (txc.getCount() != null) {
                txc.setTxCount(txc.getTxCount() + 1);
                client.inTransaction().setData().forPath(countPath, txc.toString().getBytes()).and()
                    .setData().forPath(serverPath, ZKTransactionConst.Status.COMPLETE.getBytes()).and()
                    .commit();
            }
        }

    }


    /**
     * 事务生效失败记录。
     * 
     * @throws Exception
     */
    private void faildRecord() throws Exception {
        String serverPath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR + server;
        String countPath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + ZKTransactionConst.Node.NODE_COUNT;
        String countData = new String(client.getData().forPath(countPath));
        TransactionCountDto txc = new TransactionCountDto(countData);
        if (txc.getCount() != null) {// 错误数累加1
            txc.setFailedCount(txc.getFailedCount() + 1);
            client.inTransaction().setData().forPath(countPath, txc.toString().getBytes()).and().setData()
                .forPath(serverPath, ZKTransactionConst.Status.FAILED.getBytes()).and().commit();
        }
        else {
            client.inTransaction().setData().forPath(serverPath, ZKTransactionConst.Status.FAILED.getBytes())
                .and().commit();
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
