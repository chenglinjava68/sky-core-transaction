/*
 * 文件名：ListenerSwitchDto.java
 * 版权：深圳柚安米科技有限公司版权所有
 * 描述： ListenerSwitchDto.java
 * 修改人：刘红艳
 * 修改时间：2016年8月12日
 * 修改内容：新增
 */
package com.youanmi.sky.core.transaction.dto;

import java.io.Serializable;


/**
 * 节点监听控制开关。
 * <p>
 * 
 * @author 刘红艳
 * @since 2.2.4
 */
public class ListenerSwitchDto implements Serializable {

    /**
     * 添加字段注释
     */
    private static final long serialVersionUID = 1L;
    private boolean isListener;
    private boolean isSuccess;//事务成功否

    public ListenerSwitchDto(boolean isListener) {
        super();
        this.isListener = isListener;
    }


    public boolean isListener() {
        return isListener;
    }


    public void setListener(boolean isListener) {
        this.isListener = isListener;
    }


    public boolean isSuccess() {
        return isSuccess;
    }


    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

}
