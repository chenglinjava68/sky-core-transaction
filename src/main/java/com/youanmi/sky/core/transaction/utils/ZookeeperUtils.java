/*
 * 文件名：ZookeeperUtils.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： ZookeeperUtils.java
 * 修改人：刘红艳
 * 修改时间：2016年8月12日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.utils;

import java.io.UnsupportedEncodingException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youanmi.sky.core.transaction.conf.ZKConf;


/**
 * ZK节点操作常用工具类。
 * <p>
 * 
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class ZookeeperUtils {

    private static final String CHARSET = "utf-8";
    
    /**
     * 调测日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperUtils.class);


    public static CuratorFramework createClient() {
        String host = ZKConf.getConf("transaction.zk.address");
        LOG.info("Zookeeper节点地址--->"+host);
        CuratorFramework curator = CuratorFrameworkFactory.builder().connectString(host).sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        LOG.info("Zookeeper监控对象：CuratorFramework--->"+curator);
        return curator;
    }


    public static void close(CuratorFramework client) {
        CloseableUtils.closeQuietly(client);
    }


    private static byte[] getBytes(String data) throws UnsupportedEncodingException {
        if (data == null) {
            return null;
        }
        return data.getBytes(CHARSET);
    }


    public static void createNode(CuratorFramework client, String path, String data) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            client.create().creatingParentsIfNeeded().forPath(path, getBytes(data));
        }
    }

}
