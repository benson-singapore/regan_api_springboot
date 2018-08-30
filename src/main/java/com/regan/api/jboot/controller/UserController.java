package com.regan.api.jboot.controller;

import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * 用户管理
 *
 * @action /user
 * @author zhangby
 * @date 2018/8/24 下午5:55
 */
@Controller
@RequestMapping("/user")
public class UserController {

    /**
     * 用户登录功
     * @title 登录接口
     * @param username|用户名|string|必填
     * @param password|密码|string|必填
     * @resqParam code|用户名|String|必填
     * @resqParam data|数据|object|非必填
     * @resqParam msg|消息信息|String|必填
     * @respBody {"code":"000","data":"","msg":"success"}
     * @requestType post
     * @author zhangby
     * @date 2018/6/12 下午4:23
     */
    @PostMapping("/login")
    @ResponseBody
    public String login() {
        Dict dict = Dict.create()
                .set("code", "000")
                .set("msg","登录成功");
        return JSON.toJSONString(dict);
    }

    /**
     * 用户退出功能
     * @title 退出接口
     * @resqParam code|用户名|String|必填
     * @resqParam data|数据|object|非必填
     * @resqParam msg|消息信息|String|必填
     * @respBody {"code":"000","data":"","msg":"success"}
     * @author zhangby
     * @date 2018/6/20 上午10:32
     */
    @PostMapping("/logout")
    @ResponseBody
    public String logout() {
        Dict dict = Dict.create()
                .set("code", "000")
                .set("msg","退出成功");
        return JSON.toJSONString(dict);
    }
}
