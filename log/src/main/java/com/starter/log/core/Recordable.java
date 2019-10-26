package com.starter.log.core;

import com.starter.log.emun.LogType;

import java.io.Serializable;

/**
 * @author Shoven
 * @date 2019-07-26 9:35
 */
public interface Recordable extends Serializable {

    void setModule(String module);

    void setDesc(String desc);

    void setLogType(LogType type);
}