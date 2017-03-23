/*
 * 文件名：ZKMasterTransactionHelper.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： ZKMasterTransactionHelper.java
 * 修改人：刘红艳
 * 修改时间：2016年8月12日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.helper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youanmi.commons.constants.ResultCode;
import com.youanmi.commons.exceptions.ViewExternalDisplayException;
import com.youanmi.sky.core.transaction.bizdata.service.IMasterBizdataService;
import com.youanmi.sky.core.transaction.conf.ZKConf;
import com.youanmi.sky.core.transaction.constant.ZKTransactionConst;
import com.youanmi.sky.core.transaction.dto.BizDataDto;
import com.youanmi.sky.core.transaction.dto.MasterNodeDto;
import com.youanmi.sky.core.transaction.listener.SlaveListener;
import com.youanmi.sky.core.transaction.utils.ZookeeperUtils;


/**
 * 主控方事务处理。
 * <p>
 * 
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class ZKMasterTransactionHelper {
    /**
     * 调测日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(ZKMasterTransactionHelper.class);


    /**
     * 主控方开启事务。
     * <p>
     * 1.创建主控方事务协调节点。<br>
     * 2.监听被控方事务完成状态。<br>
     * 
     * @param master 事务节点数据
     * @param masterBizdataService 业务审核数据操作对象
     * @param server 主控方业务标识，如商户注册：sreg，等
     * @param businessData 业务审核数据
     * @throws ViewExternalDisplayException
     */
    public static void start(MasterNodeDto master, IMasterBizdataService masterBizdataService, String server,
            BizDataDto businessData) {
        LOG.info("主控方开启事务 开始:" + master.toString());
        try {
            createMasterNode(master);
            // 1.创建主控方事务协调节点。
            SlaveListener.listener(master, masterBizdataService, server);// 2.监听被控方事务完成状态
        }
        catch (Exception e) {
            if (e instanceof ViewExternalDisplayException) {
                throw (ViewExternalDisplayException) e;
            }
            LOG.error("ZK操作异常！", e);
            throw new ViewExternalDisplayException(ResultCode.Sys.ZK_TRANSACTION_ERROR, e);
        }
        if (businessData != null) {
            masterBizdataService.save(businessData);// 3.存贮审核数据
        }
        LOG.info("主控方开启事务 结束:" + master.toString());
    }


    /**
     * 创建主控方事务节点.
     * 
     * @param master
     * @throws Exception
     */
    private static void createMasterNode(MasterNodeDto master) throws Exception {
        CuratorFramework client = ZookeeperUtils.createClient();
        try {
            client.start();
            String path = ZKConf.getRootPath() + master.getObjId();
            // 1.如果要锁，则检测锁
            if (master.isLock()) {
                if (client.checkExists().forPath(path) != null) {
                    byte[] b = client.getData().forPath(path);
                    if (b != null) {
                        String isLock = new String(b).split(ZKTransactionConst.Node.COUNT_SPLIT_CHAR)[0];
                        if (isLock != null && isLock.equals(Boolean.TRUE.toString())) {
                            LOG.error("资源锁定中！resource:" + master.getObjId());
                            throw new ViewExternalDisplayException(ResultCode.Sys.RESOURCE_LOCKED);
                        }
                    }
                }
            }
            // 2.创建请求节点
            ZookeeperUtils.createNode(client, path, Boolean.toString(master.isLock()));// 对象节点可能先存在.不用和requestId同事务构建。
            String requstIdPath = path + ZKTransactionConst.Node.SPLIT_CHAR + master.getRequestId();
            String countPath =
                    requstIdPath + ZKTransactionConst.Node.SPLIT_CHAR + ZKTransactionConst.Node.NODE_COUNT;
            CuratorTransaction ct = client.inTransaction();// 开启事务
            if (master.isLock()) {// 如果是锁定，则把数据标记为锁定
                ct.setData().forPath(
                    path,
                    (master.isLock() + ZKTransactionConst.Node.COUNT_SPLIT_CHAR + master.getRequestId())
                        .getBytes());
            }
            ct.create().forPath(requstIdPath, ZKTransactionConst.Status.PENDING.getBytes()).and().create()
                .forPath(countPath, null).and().commit();
        }
        finally {
            ZookeeperUtils.close(client);
        }
    }


    /**
     * 主控方提交事务。
     * <p>
     * 主控方事务完成，标记requestid事务完成，data=C)。
     * 
     * @throws ViewExternalDisplayException
     */
    public static void commit(MasterNodeDto master, IMasterBizdataService masterBizdataService) {
        masterBizdataService.commit(master.getRequestId());// 放在前面
        CuratorFramework client = ZookeeperUtils.createClient();
        try {
            client.start();
            String path = ZKConf.getRootPath() + master.getObjId();
            String requstIdPath = path + ZKTransactionConst.Node.SPLIT_CHAR + master.getRequestId();
            client.setData().forPath(requstIdPath, ZKTransactionConst.Status.COMPLETE.getBytes());
        }
        catch (Exception e) {
            LOG.error("ZK操作异常！", e);
            throw new ViewExternalDisplayException(ResultCode.Sys.ZK_TRANSACTION_ERROR, e);
        }
        finally {
            ZookeeperUtils.close(client);
        }
    }


    /**
     * 取消事务。
     * <p>
     * 1.删除请求节点，解除锁。<br>
     * 2.标记主控方事务失败。
     * 
     * @param master
     */
    public static void cancel(MasterNodeDto master, IMasterBizdataService transactionResultNotify,
            String server) {
        // 1：删除请求节点及解锁对象
        boolean del = deleteRequestIdNodeByCancel(master);
        // 2.修改主控方事务失败
        if (del) {// 仅当取消事务了才允许标记失败
            transactionResultNotify.fail(master.getRequestId(), server);
        }
    }


    /**
     * 主控方事务失败时， 删除请求节点。
     * 
     * @return {@link Boolean} 删除成功则返回true,代表事务取消成功，否则代表无法取消事务
     */
    private static boolean deleteRequestIdNodeByCancel(MasterNodeDto master) {
        CuratorFramework client = ZookeeperUtils.createClient();
        String deletePath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId();
        try {
            client.start();
            String objPath = ZKConf.getRootPath() + master.getObjId();
            byte[] masterStatus = client.getData().forPath(deletePath);
            String requestStatus = masterStatus == null ? null : new String(masterStatus);
            // 仅当主控方事务状态未完成时，才删除，如果已完成则不允许删除,数据会在完成整体事务时才删除
            if (requestStatus == null || !(requestStatus.equals(ZKTransactionConst.Status.COMPLETE))) {
                if (master.isLock()) {// 1.锁了资源则解锁
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
                return true;
            }
        }
        catch (Exception e) {// 删节点的异常catch，避免抛出的异常影响业务操作
            LOG.info("删除ZK节点失败：" + deletePath, e);
        }
        finally {
            ZookeeperUtils.close(client);
        }
        return false;
    }


    /**
     * 删除请求节点，及解锁对象。 <br/>
     * 用于在数据审核时，审核完成后删除该ZK节点。
     * 
     * @return {@link Boolean} 删除成功则返回true,代表事务取消成功，否则代表无法取消事务
     */
    public static void deleteRequestIdNode(MasterNodeDto master) {
        CuratorFramework client = ZookeeperUtils.createClient();
        String deletePath =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId();
        try {
            client.start();
            String objPath = ZKConf.getRootPath() + master.getObjId();
            byte[] bd = client.getData().forPath(objPath);
            String lockStr = (bd == null) ? null : new String(bd);
            // 仅当锁对象为该删除的事务节点的时，才解除对象锁
            if (lockStr != null
                    && (lockStr.equals(Boolean.TRUE.toString() + ZKTransactionConst.Node.COUNT_SPLIT_CHAR
                            + master.getRequestId()))) {
                LOG.info("解锁：" + deletePath);
                CuratorTransaction ct = client.inTransaction();// 开启事务
                ct.setData().forPath(objPath, Boolean.FALSE.toString().getBytes()).and().commit();
            }
            else {
                // do nothing
            }
            LOG.info("删除节点：" + deletePath);
            // 2.删了请求节点(子节点全部删除无法在事务中执行？)
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(deletePath);
            if (client.checkExists().forPath(deletePath) != null) {
                client.delete().forPath(deletePath);
            }
        }
        catch (Exception e) {
            LOG.info("删除ZK节点失败：" + deletePath, e);
            throw new ViewExternalDisplayException(ResultCode.Sys.ZK_TRANSACTION_ERROR, e);
        }
        finally {
            ZookeeperUtils.close(client);
        }
    }
}
