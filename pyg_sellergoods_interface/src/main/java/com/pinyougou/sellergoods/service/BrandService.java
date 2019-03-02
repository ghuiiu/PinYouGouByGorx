package com.pinyougou.sellergoods.service;

import com.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;

public interface BrandService {

     List<TbBrand> findAll();

    /**
     * 需求：品牌分页查询展示
     * 参数：int pageSize,int pageNo
     * 返回值：分页包装类对象
     */
    PageResult findPage(int pageSize,int pageNo);

    /**
     * 需求：插入品牌数据
     * 返回值：void
     */
    public  void  add(TbBrand brand);

    /**
     * 修改
     */
    public void update(TbBrand brand);
    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);


}
