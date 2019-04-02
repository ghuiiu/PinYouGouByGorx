package com.pinyougou.user.service.impl;
import java.util.List;

import com.pinyougou.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbAddressMapper;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAddressExample;
import com.pinyougou.pojo.TbAddressExample.Criteria;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	private TbAddressMapper addressMapper;


	/**
	 * 返回全部列表
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public List<TbAddress> findListByUserId(String userId) {
		TbAddressExample example = new TbAddressExample();
		example.createCriteria().andUserIdEqualTo(userId);
		return addressMapper.selectByExample(example);
	}
}













