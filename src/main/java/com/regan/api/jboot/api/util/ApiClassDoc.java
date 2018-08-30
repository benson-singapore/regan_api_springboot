package com.regan.api.jboot.api.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 注解数据集
 *
 * @author zhangby
 * @date 2018/5/23 下午4:13
 */
public class ApiClassDoc {

    private String action;
    private String method;

    public ApiClassDoc setAction(String action) {
        this.action = action;
        return this;
    }

    public ApiClassDoc setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * 注解数据集
     */
    private Map<String, Dict> apiDataMap = Maps.newLinkedHashMap();

    public Map<String, Dict> getApiDataMap() {
        return apiDataMap;
    }

    public void setApiDataMap(Map<String, Dict> apiDataMap) {
        this.apiDataMap = apiDataMap;
    }

    /**
     * 添加解析数据
     * @param key key值
     * @param val val值
     * @return
     */
    public ApiClassDoc putClassDoc(String key, Dict val) {
        apiDataMap.put(key, val);
        return this;
    }

    /**
     * 获取controller注解
     * @return
     */
    public Dict getControllerInfo() {
        if (StrUtil.isNotBlank(this.action)) {
            return Convert.convert(Dict.class,this.apiDataMap.get(this.action));
        }
        return null;
    }

    /**
     * 获取方法上的注解
     * @return
     */
    public Dict getMethodInfo() {
        if (StrUtil.isNotBlank(this.action) && StrUtil.isNotBlank(this.method)) {
            return Convert.convert(Dict.class, Convert.convert(Map.class,getControllerInfo().get("methods")).get(this.method));
        }
        return null;
    }
}
