package com.pinyougou.user.controlle;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.PhoneFormatCheckUtils;

/**
 * @Auther: Gorx
 * @Date: 2019/3/19 20:56
 * @Description:
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Reference(timeout = 100000)
    private UserService userService;

    /**
     * 增加
     * @param user
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbUser user, String code){
        //需要在注册用户之前验证短信验证码
        boolean smsCode = userService.checkSmsCode(user.getPhone(), code);
        //返回的是false,证明短信验证码输入错误
        if (!smsCode) {
            return new Result(false,"短信验证码输入错误！！！");
        }

        try {
            userService.add(user);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }

    }

    @RequestMapping("/sendSms")
    public Result sendSms(String phone) {
        boolean legal = PhoneFormatCheckUtils.isPhoneLegal(phone);

        if (!legal) {
            return new Result(false, "手机号格式不正确！！！");
        }

        try {
            userService.sendSms(phone);
            return new Result(true, "发送短信成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "发送短信失败");
        }
    }
}



















