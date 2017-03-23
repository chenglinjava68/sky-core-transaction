/*
 * 文件名：SlaveTransactionHandle.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： SlaveTransactionHandle.java
 * 修改人：刘红艳
 * 修改时间：2016年8月13日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.bizdata.service;

import com.youanmi.sky.core.transaction.dto.BizDataDto;


/**
 * 被控方审核数据操作接口。
 * <p>
 * 
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public interface ISlaveBizdataService {

    /**
     * 存贮被控方业务审核数据。
     * 
     * @param dto
     */
    public void save(BizDataDto dto);


    /**
     * 事务确认。
     * <p>
     * 1.将预处理数据生效变为可用的业务数据。<br>
     * 2.将审核数据的状态修改为成功。<br>
     * 
     * @param requestId 请求ID
     * @param server 服务
     * */
    public void confirm(String requestId, String server);


    /**
     * 事务取消，将预处理的数据取消变为不可用的业务数据。 unuse.
     * 
     * @param requestId 请求ID
     * @param server 服务
     * */
    public void cancel(String requestId, String server);

}
