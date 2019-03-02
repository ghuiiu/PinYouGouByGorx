package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;

import java.util.List;

public interface TbBrandMapper {

    public List<TbBrand> findAll();

    public void add(TbBrand brand);

    public TbBrand findOne(Long id);

    public void update(TbBrand brand);

    public void dele(Long id);
}
