package com.pinyougou.user.service;
import java.util.List;
import com.pinyougou.pojo.TbAddress;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface AddressService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbAddress> findListByUserId(String userId);


	
}
