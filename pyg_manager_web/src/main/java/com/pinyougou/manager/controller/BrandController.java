package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.entity.PageResult;
import com.entity.Result;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: Gorx
 * @Date: 2019/2/28 09:03
 * @Description:
 */
@RequestMapping("/brand")
@RestController
public class BrandController {
    @Reference
   private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(int pageSize, int pageNo){
        return brandService.findPage(pageSize,pageNo);
    }

    @RequestMapping("/add")
    public Result add(TbBrand tbBrand){
        try {
            brandService.add(tbBrand);
            return new Result(true,"新增成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"新增失败");
        }
    }

    @RequestMapping("/update")
    public Result update(TbBrand tbBrand){
        try {
            brandService.update(tbBrand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }


}


















