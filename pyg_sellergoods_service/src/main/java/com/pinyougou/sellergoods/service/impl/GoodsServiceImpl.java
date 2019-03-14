package com.pinyougou.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsDescService;
import entity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//封装对象中获取商品对象保存数据库
		TbGoods tbGoods = goods.getGoods();
		//新增商品默认为草稿状态
		tbGoods.setAuditStatus("0");
        //将商品对象插入进数据库
        goodsMapper.insert(tbGoods);
		//将商品详情插入数据库
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		//注意一对一关系的维护
		//注意dao层新增后获取新增对象的id需要配置Mybatis xml文件
		goodsDesc.setGoodsId(tbGoods.getId());
		goodsDescMapper.insert(goodsDesc);

		//判断商品规格如果是启用状态
		if ("1".equals(tbGoods.getIsEnableSpec())) {
			//处理sku列表集合
			List<TbItem> itemList = goods.getItemList();
			for (TbItem item : itemList) {
				//price,num,isDefault,spec,status 页面传入的值
				//title是商品名称+spec中的所有value值
				String titile = tbGoods.getGoodsName();
				Map<String,String> map = JSON.parseObject(item.getSpec(), Map.class);
				//循环spec中的值{"机身内存":"16G","网络":"联通3G"} 拼接title
				for (String s : map.keySet()) {
					titile += " "+ map.get(s);
				}
				//设置title
				item.setTitle(titile);
				//商品卖点(就是商品的描述)
				item.setSellPoint(tbGoods.getCaption());
				//保存商品图片集合中的图片地址，取第一张(在商品详情对象中)
				List<Map> images = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
				//如果图片存在
				if (images.size() > 0) {
					//取第一个对象的图片地址
					item.setImage(images.get(0).get("url").toString());
				}
				//需要保存三级分类id
				item.setCategoryid(tbGoods.getCategory3Id());
				//设置创建时间
				item.setCreateTime(new Date());
				//设置商品更新时间
				item.setUpdateTime(new Date());
				//设置商品id
				item.setGoodsId(tbGoods.getId());

				//设置商家的id,注意确认已经从springSecurity中获取到sellerid封装到goods中
				item.setSellerId(tbGoods.getSellerId());

				//设置分类名称
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
				item.setCategory(itemCat.getName());

				//设置品牌名称
				TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
				item.setBrand(brand.getName());

				//设置seller的name
				TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
				item.setSeller(seller.getName());

				//注意传入的status和isDefault的长度！！！！
				itemMapper.insert(item);
			}
		}else {
			//没启用规格
            TbItem item = new TbItem();
            // 商品名称直接设置到sku对象上
            item.setTitle(tbGoods.getGoodsName());
            //商品卖点(就是商品的描述)
            item.setSellPoint(tbGoods.getCaption());
            //保存商品图片集合中的图片地址，取第一张(在商品详情对象中)
            List<Map> images = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
            //如果图片存在
            if (images.size() > 0) {
                //取第一个对象的图片地址
                item.setImage(images.get(0).get("url").toString());
            }
            //需要保存三级分类id
            item.setCategoryid(tbGoods.getCategory3Id());
            //设置创建时间
            item.setCreateTime(new Date());
            //设置商品更新时间
            item.setUpdateTime(new Date());
            //设置商品id
            item.setGoodsId(tbGoods.getId());

            //设置商家的id,注意确认已经从springSecurity中获取到sellerid封装到goods中
            item.setSellerId(tbGoods.getSellerId());

            //设置分类名称
            TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            item.setCategory(itemCat.getName());

            //设置品牌名称
            TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
            item.setBrand(brand.getName());

            //设置seller的name
            TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
            item.setSeller(seller.getName());
            //将spec，status，isDefault，price，num设置默认值（之前是页面传入）
            item.setPrice(tbGoods.getPrice());
            item.setStatus("1");
            item.setIsDefault("1");
            item.setNum(99999);
            itemMapper.insert(item);
        }
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbGoods goods){
		goodsMapper.updateByPrimaryKey(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbGoods findOne(Long id){
		return goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("0");
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

			//只查询未删除的商品
			criteria.andIsDeleteIsNull();
			if(goods!=null){
				if(goods.getSellerId()!=null && goods.getSellerId().length()>0){  //商家id查询
					criteria.andSellerIdEqualTo(goods.getSellerId());
				}
				if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){  //名称查询
					criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
				}
				if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){ //状态查询
					criteria.andAuditStatusEqualTo(goods.getAuditStatus());
				}
			}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 批量修改状态
	 *
	 * @param ids
	 * @param status
	 */
	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}




}
















