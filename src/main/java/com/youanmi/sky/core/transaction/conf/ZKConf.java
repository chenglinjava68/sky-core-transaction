/*
 * 文件名：ResultCodeConf.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 修改人：tanguojun
 * 修改时间：2016年4月26日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.youanmi.commons.utils.cache.ApplicationConfCache;
import com.youanmi.sky.core.transaction.constant.ZKTransactionConst;
import com.youanmi.sky.core.transaction.helper.ZKSlaveTransactionHelper;


/**
 * ZK配置
 * 
 * @author tanguojun
 * @since 2.2.4
 */
@Service
public class ZKConf {

    /**
     * 配置文件名称
     */
    public static final String FILE_NAME = "common.transaction.zk.properties";
    /** root节点 */
    public static final String ROOT_PATH = "transaction.zk.root.path";

    /**
     * 调测日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(ZKConf.class);
   
    private static boolean init=false;
    /**
     * 初始化加载配置文件。
     * @author 刘红艳  2016年8月29日 add
     */
    public static void init(){
        LOG.info("初始化ZK配置。" + FILE_NAME);
        ApplicationConfCache.initConf(FILE_NAME);
        init = true;
    }

    /**
     *
     * 获取ZK系统配置的值
     *
     * @param key 配置键
     * @return value
     */
    public static String getConf(String key) {

        if(!init){
            init();
        }
        return ApplicationConfCache.getValue(FILE_NAME, key);
    }


    /**
     * ZKroot 节点。
     * 
     * @return
     * @author 刘红艳 2016年8月29日 add
     */
    public static String getRootPath() {
        String p = getConf(ROOT_PATH);
        if (!ROOT_PATH.startsWith(ZKTransactionConst.Node.SPLIT_CHAR)) {
            p = ZKTransactionConst.Node.SPLIT_CHAR + p;
        }
        if (!ROOT_PATH.endsWith(ZKTransactionConst.Node.SPLIT_CHAR)) {
            p = p + ZKTransactionConst.Node.SPLIT_CHAR;
        }
        LOG.info("Root Path:"+p);
        return p;
    }
}
