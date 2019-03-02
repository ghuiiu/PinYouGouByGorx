package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.entity.PageResult;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {
        return brandMapper.findAll();
    }

    /**
     * 需求：品牌分页查询展示
     * 参数：int pageSize, int pageNo
     * 返回值：分页包装类对象
     *
     * @param pageSize
     * @param pageNo
     */
    @Override
    public PageResult findPage(int pageSize, int pageNo) {
        //使用分页插件并查询全部商品分类
        PageHelper.startPage(pageNo,pageSize);
        Page page = (Page) brandMapper.findAll();
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 需求：插入品牌数据
     * 返回值：void
     *
     * @param brand
     */
    @Override
    public void add(TbBrand brand) {
        brandMapper.add(brand);
    }

    /**
     * 修改
     *
     * @param brand
     */
    @Override
    public void update(TbBrand brand) {
        brandMapper.update(brand);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandMapper.dele(id);
        }
    }

}
















