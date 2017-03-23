/*
 * 文件名：TransactionCountDto.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： TransactionCountDto.java
 * 修改人：刘红艳
 * 修改时间：2016年8月12日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.dto;

import java.io.Serializable;

import com.youanmi.sky.core.transaction.constant.ZKTransactionConst;


/**
 * 事务计数器。
 * <p>
 * 
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class TransactionCountDto implements Serializable {

    /**
     * 添加字段注释
     */
    private static final long serialVersionUID = 1L;
    /** 总服务数 */
    private Integer count;
    /** 完成数据生效的服务数 */
    private Integer txCount;
    /** 已失败数据生效的总数 */
    private Integer failedCount;


    public TransactionCountDto() {
        super();
    }


    /**
     * 
     * 构造函数。
     * 
     * @param data 规格：count:txCount 如，5:4
     */
    public TransactionCountDto(String data) {
        super();
        if (data == null || data.trim().equals("")) {
            System.out.println("Count data is null");
            return;
        }
        String[] s = data.split(ZKTransactionConst.Node.COUNT_SPLIT_CHAR);
        if (s.length != 3) {
            System.out.println("Count data is unnormal." + data);
            return;
        }
        else {
            Integer c = Integer.parseInt(s[0]);
            Integer txC = Integer.parseInt(s[1]);
            Integer fC = Integer.parseInt(s[2]);
            this.count = c;
            this.txCount = txC;
            this.failedCount=fC;
        }
    }


    public TransactionCountDto(Integer count, Integer txCount) {
        super();
        this.count = count;
        this.txCount = txCount;
    }


    public Integer getCount() {
        return count;
    }


    public void setCount(Integer count) {
        this.count = count;
    }


    public Integer getTxCount() {
        return txCount;
    }


    public void setTxCount(Integer txCount) {
        this.txCount = txCount;
    }


    public Integer getFailedCount() {
        return failedCount;
    }


    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }


    @Override
    public String toString() {
        if (count == null) {
            return super.toString();
        }
        return count + ":" + txCount + ":" + failedCount;
    }

}
