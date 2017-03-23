/*
 * 文件名：ZKTransactionConst.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： ZKTransactionConst.java
 * 修改人：刘红艳
 * 修改时间：2016年8月12日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.constant;

/**
 * 分布式事务常量。
 * <p>
 * 
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public interface ZKTransactionConst {
    interface Status {
        /** 预处理 */
        public static final String PENDING = "P";
        /** 完成 */
        public static final String COMPLETE = "C";
        /** 失败 */
        public static final String FAILED = "F";
    }

    interface Node {
        /** 节点分割符 */
        public static final String SPLIT_CHAR = "/";
        /** ZK事务计数器节点名称 */
        public static final String NODE_COUNT = "count";
        /** ZK事务计数器数据分隔符 */
        public static final String COUNT_SPLIT_CHAR = ":";

    }
}
