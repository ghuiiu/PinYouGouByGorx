package com.pinyougou.manager.controller;

import entity.PageResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: Gorx
 * @Date: 2019/3/6 09:55
 * @Description:
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    /**
     * 需求：在登录成功页面展示登录用户名
     */
    @RequestMapping("/showName")
    public Map showName(){
        //获取登录用户信息
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap<>();
        map.put("loginName",name);
        return map;
    }
}
