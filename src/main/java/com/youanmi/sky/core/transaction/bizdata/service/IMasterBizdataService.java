/*
 * 文件名：IMasterBizdataService.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： IMasterBizdataService.java
 * 修改人：刘红艳
 * 修改时间：2016年8月13日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.bizdata.service;

import com.youanmi.sky.core.transaction.dto.BizDataDto;


/**
 * 主控方事务审核数据操作接口。
 * <p>
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public interface IMasterBizdataService {

    /**
     * 存贮主控方业务审核数据。
     * 
     * @param dto
     */
    public void save(BizDataDto dto);


    /**
     * 整体事务成功(主控方和被控方都完成)，记录成功信息到数据库。
     * 
     * @param requestId
     * @param server
     */
    public void success(String requestId, String server);


    /**
     * 事务失败，记录失败信息到数据库。
     * 
     * @param requestId
     * @param server
     */
    public void fail(String requestId, String server);


    /**
     * 主控方事务完成（单方面的只针对主控方的事务）。
     * 
     * @param requestId
     * @author 刘红艳 2016年8月18日 add
     */
    public void commit(String requestId);

}
