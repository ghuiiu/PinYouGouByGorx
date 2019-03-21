package com.pinyougou.user.service.impl;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.HttpClientUtil;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		user.setCreated(new Date());//创建日期
		user.setUpdated(new Date());//修改日期

		//先将密码进行加密处理
		String password = DigestUtils.md5Hex(user.getPassword());//对密码加密，apache框架的

		user.setPassword(password);

		userMapper.insert(user);		
	}

	/**
	 * 发送短信服务
	 *
	 * @param phone
	 */
	@Override
	public void sendSms(String phone) {
		try {
			//通过RandomStringUtils工具类生成指定个数的随机数
			String code = RandomStringUtils.randomNumeric(6);
			//将验证码存入redis中,小key是手机号，内容是验证码
			redisTemplate.boundHashOps("phoneCode").put(phone,code);

			System.out.println("短信验证码是===="+code);

			String phoneCode = (String) redisTemplate.boundHashOps("phoneCode").get(phone);
			System.out.println("==redis存的code="+phoneCode);

			//准备httpclient发送
			HttpClientUtil util = new HttpClientUtil("http://localhost:9002/sms.do?phone=" + phone + "&code=" + code);
			//发送get请求
			util.get();
			//获取请求后的返回值
			String content = util.getContent();
			System.out.println(content);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 验证手机短信验证码
	 *
	 * @param phone
	 * @param inputCode
	 */
	@Override
	public boolean checkSmsCode(String phone, String inputCode) {
		//根据手机号获取验证码
		String phoneCode = (String) redisTemplate.boundHashOps("phoneCode").get(phone);
		//如果redis中有验证码，并且输入的验证码和redis的验证码一致
		if (phoneCode!=null && inputCode.equals(phoneCode)) {
			return true;
		}
		return false;
	}


}












