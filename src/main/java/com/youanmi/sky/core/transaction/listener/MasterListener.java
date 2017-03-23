/*
 * 文件名：MasterListener.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： MasterListener.java
 * 修改人：刘红艳
 * 修改时间：2016年8月13日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youanmi.sky.core.transaction.bizdata.service.ISlaveBizdataService;
import com.youanmi.sky.core.transaction.conf.ZKConf;
import com.youanmi.sky.core.transaction.constant.ZKTransactionConst;
import com.youanmi.sky.core.transaction.dto.MasterNodeDto;


/**
 * 监听主控方事务完成状态。
 * <p>
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class MasterListener {
    /**
     * 调测日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(MasterListener.class);


    /**
     * 异步监听。
     * 
     * @param master 监听的主控方节点
     * @param slaveBizdataService 被控方事务数据操作这
     * @param server 服务名,如:注册/修改 能关联到具体的业务的标识
     * @throws Exception
     */
    public static void listener(MasterNodeDto master, ISlaveBizdataService slaveBizdataService, String server)
            throws Exception {
        String node =
                ZKConf.getRootPath() + master.getObjId() + ZKTransactionConst.Node.SPLIT_CHAR
                        + master.getRequestId() + ZKTransactionConst.Node.SPLIT_CHAR + server;
        LOG.info("开始监听事务：" + node);
        MasterListenerThread t1 = new MasterListenerThread(master, slaveBizdataService, server);
        t1.start();
        LOG.info("已监听事务：" + node);
    }
}
