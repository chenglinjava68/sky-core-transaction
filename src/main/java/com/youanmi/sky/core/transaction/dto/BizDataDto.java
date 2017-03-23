package com.youanmi.sky.core.transaction.dto;

import java.io.Serializable;


/**
 * 业务数据对象
 * 
 * @author 张秋平 20160808
 *
 */
public class BizDataDto implements Serializable {

    private static final long serialVersionUID = 3063213849569626498L;
    private Long id;

    private String caller;// 调用方

    private String requestId;// 请求id，保证唯一性

    private String znodeEntity;// zk节点对象（一般为修改的手机号码）

    private String status;// P:进行中;C成功F失败

    private String ip;// 调用服务的ip地址

    private String serviceType;// 服务类型

    private String callMethod;// 调用方法的类路径

    private Long callUser;// 调用者id

    private String requestData;// 请求的详细数据，或者请求报文

    private String callee;// 别调用者

    private String responseData;// 响应数据

    private String txResultDetail;// 失败详情

    private String txStatus;// 事务最终状态

    private Long createTime;// 创建时间

    private Long createId;// 创建者

    private Long updateTime;// 修改人

    private Long updateId;// 修改者

    private String resultDetail;//被控方结果详情

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    /**
	 * @return the resultDetail
	 */
	public String getResultDetail() {
		return resultDetail;
	}


	/**
	 * @param resultDetail the resultDetail to set
	 */
	public void setResultDetail(String resultDetail) {
		this.resultDetail = resultDetail;
	}


	public String getCaller() {
        return caller;
    }


    public void setCaller(String caller) {
        this.caller = caller;
    }


    public String getRequestId() {
        return requestId;
    }


    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    public String getZnodeEntity() {
        return znodeEntity;
    }


    public void setZnodeEntity(String znodeEntity) {
        this.znodeEntity = znodeEntity;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public String getIp() {
        return ip;
    }


    public void setIp(String ip) {
        this.ip = ip;
    }


    public String getServiceType() {
        return serviceType;
    }


    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }


    public String getCallMethod() {
        return callMethod;
    }


    public void setCallMethod(String callMethod) {
        this.callMethod = callMethod;
    }


    public Long getCallUser() {
        return callUser;
    }


    public void setCallUser(Long callUser) {
        this.callUser = callUser;
    }


    public String getRequestData() {
        return requestData;
    }


    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }


    public String getCallee() {
        return callee;
    }


    public void setCallee(String callee) {
        this.callee = callee;
    }


    public String getResponseData() {
        return responseData;
    }


    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }


    public String getTxResultDetail() {
        return txResultDetail;
    }


    public void setTxResultDetail(String txResultDetail) {
        this.txResultDetail = txResultDetail;
    }


    public String getTxStatus() {
        return txStatus;
    }


    public void setTxStatus(String txStatus) {
        this.txStatus = txStatus;
    }


    public Long getCreateTime() {
        return createTime;
    }


    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }


    public Long getCreateId() {
        return createId;
    }


    public void setCreateId(Long createId) {
        this.createId = createId;
    }


    public Long getUpdateTime() {
        return updateTime;
    }


    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


    public Long getUpdateId() {
        return updateId;
    }


    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

}
