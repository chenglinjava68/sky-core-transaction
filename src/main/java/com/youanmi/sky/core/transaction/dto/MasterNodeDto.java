/*
 * 文件名：MasterNodeDto.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： MasterNodeDto.java
 * 修改人：刘红艳
 * 修改时间：2016年8月12日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.dto;

import java.io.Serializable;


/**
 * 主控方事务节点对象。
 * <p>
 * 
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class MasterNodeDto implements Serializable {

    /**
     * 添加字段注释
     */
    private static final long serialVersionUID = 1L;
    /** 对象标识 */
    private String objId;
    /** 请求ID，必须全局唯一 */
    private String requestId;
    /** 是否需要锁 */
    private Boolean isLock;


    /**
     * 被控方构造函数。
     * 
     * @param objId 锁对象标识
     * @param requestId 请求ID
     */
    public MasterNodeDto(String objId, String requestId) {
        super();
        this.objId = objId;
        this.requestId = requestId;
    }


    /**
     * 主控方构造函数。
     * 
     * @param objId 锁对象标识
     * @param requestId 请求ID
     * @param isLock 主控方则一定要非空
     */
    public MasterNodeDto(String objId, String requestId, Boolean isLock) {
        super();
        this.objId = objId;
        this.requestId = requestId;
        this.isLock = isLock;
    }


    public String getObjId() {
        return objId;
    }


    public void setObjId(String objId) {
        this.objId = objId;
    }


    public String getRequestId() {
        return requestId;
    }


    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    public Boolean getIsLock() {
        return isLock;
    }


    public Boolean isLock() {
        return isLock == null ? false : isLock;
    }


    public void setIsLock(Boolean isLock) {
        this.isLock = isLock;
    }


    @Override
    public String toString() {
        // Auto-generated method stub
        return "isLock:" + isLock + "/objId:" + objId + "/requestId:" + requestId + "/";
    }
}
