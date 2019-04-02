package com.pinyougou.cart.controller;
import java.util.List;

import com.pinyougou.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;


/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/address")
public class AddressController {

	@Reference
	private AddressService addressService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findListByUserId")
	public List<TbAddress> findListByUserId(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		return addressService.findListByUserId(name);
	}
	

	
}

















