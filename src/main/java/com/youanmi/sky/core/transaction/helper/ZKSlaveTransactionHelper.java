/*
 * 文件名：ZKSlaveTransactionHelper.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： ZKSlaveTransactionHelper.java
 * 修改人：刘红艳
 * 修改时间：2016年8月13日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.helper;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youanmi.commons.constants.ResultCode;
import com.youanmi.commons.exceptions.ViewExternalDisplayException;
import com.youanmi.sky.core.transaction.bizdata.service.ISlaveBizdataService;
import com.youanmi.sky.core.transaction.conf.ZKConf;
import com.youanmi.sky.core.transaction.constant.ZKTransactionConst;
import com.youanmi.sky.core.transaction.dto.BizDataDto;
import com.youanmi.sky.core.transaction.dto.MasterNodeDto;
import com.youanmi.sky.core.transaction.dto.TransactionCountDto;
import com.youanmi.sky.core.transaction.listener.MasterListener;
import com.youanmi.sky.core.transaction.utils.ZookeeperUtils;


/**
 * 被控方事务处理。
 * <p>
 * 
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class ZKSlaveTransactionHelper {
    /**
     * 调测日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(ZKSlaveTransactionHelper.class);


    /**
     * 被控方开启事务。
     * <p>
     * 1.创建被控方服务节点。<br>
     * 2.监听requestId节点，即监听主控方事务状。<br>
     * 
     * @param master 主控方节点
     * @param slaveBizdataService 业务审核数据操作对象
     * @param server 被控方服务节点
     * @param businessData 审核数据
     * @throws Exception
     */
    public static void start(MasterNodeDto master, ISlaveBizdataService slaveBizdataService, String server,
            BizDataDto businessData) {
        LOG.info("被控方开启事务 开始:" + master.toString() + server);
        try {
            createSlaveNode(master, server);// 1.创建被控方服务节点。
            MasterListener.listener(master, slaveBizdataService, server);// 2.监听主控方事务
        }
        catch (Exception e) {
            LOG.error("ZK操作异常！", e);
            throw new ViewExternalDisplayException(ResultCode.Sys.ZK_TRANSACTION_ERROR, e);
        }
        if (businessData != null) {
            slaveBizdataService.save(businessData);// 3.存贮审核数据
        }
        LOG.info("被控方开启事务 结束:" + master.toString() + server);
    }


    /**
     * 创建被控方事务控制节点。
     * 
     * @param master 主控方节点
     * @param server 被控方节点服务名。
     * @throws Exception
     */
    private static void createSlaveNode(MasterNodeDto master, String server) throws Exception {
        String path =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId();
        LOG.info("即将创建ZK节点目录--->"+path);
        CuratorFramework client = ZookeeperUtils.createClient();
        try {
            client.start();
            if (client.checkExists().forPath(path) == null) {
                throw new RuntimeException("请求事务节点不存在，无法进行事务控制！");
            }
            else {
                String serverPath = path + ZKTransactionConst.Node.SPLIT_CHAR + server;
                String countPath =
                        ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                                + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR
                                + ZKTransactionConst.Node.NODE_COUNT;
                String cd = incrementCount(master, client);
                client.inTransaction().create()
                    .forPath(serverPath, ZKTransactionConst.Status.PENDING.getBytes()).and().setData()
                    .forPath(countPath, cd.getBytes()).and().commit();
            }
            LOG.info("创建ZK节点目录--->"+path+"--->成功");
        }
        finally {
            ZookeeperUtils.close(client);
        }
    }


    /**
     * 完成事务总数+1。
     * 
     * @throws Exception
     */
    private static String incrementCount(MasterNodeDto master, CuratorFramework client) throws Exception {
        String countPath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + ZKTransactionConst.Node.NODE_COUNT;
        byte[] cb = client.getData().forPath(countPath);
        TransactionCountDto txc = null;
        txc = (cb == null ? new TransactionCountDto() : new TransactionCountDto(new String(cb)));
        if (txc.getCount() == null) {
            txc.setCount(0);
            txc.setTxCount(0);
            txc.setFailedCount(0);
        }
        txc.setCount(txc.getCount() + 1);
        return txc.toString();
    }
}
