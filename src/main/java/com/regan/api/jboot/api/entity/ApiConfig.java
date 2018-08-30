package com.regan.api.jboot.api.entity;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * API配置信息
 * 
 * @author zhangby
 * @date 2018/8/24 下午5:16
 */
public class ApiConfig {
    /** 指定需要的过滤Controller */
    private List<String> packages = Lists.newArrayList();
    private List<String> filters = Lists.newArrayList();
    private String theme = "dark";

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
