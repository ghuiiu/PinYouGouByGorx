package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: Gorx
 * @Date: 2019/3/21 09:54
 * @Description:
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 根据商品信息把商品添加到购物车集合中
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.首先根据id查询TbItem
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = findCartFromCartList(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart==null) {
            //4.1 新建购物车对象
            cart = new Cart();
            //将item对象变成OrderItem
            TbOrderItem orderItem = itemToOrderItem(item, num);
            //将orderItem存到cart.orderItemList
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);

            //4.2 将新建的购物车对象添加到购物车列表
            cart.setSellerId(sellerId);//设置商家id
            cart.setSellerName(item.getSeller());//设置商家名称
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = findOrderItemFromList(cart.getOrderItemList(),itemId);
            //5.1. 如果没有，新增购物车明细
            if (orderItem==null){
                orderItem = itemToOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                //重新设置总金额
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));
                // 如果数量操作后小于等于0，则移除
                if (orderItem.getNum()<1){
                    cart.getOrderItemList().remove(orderItem);
                }

                //如果该商家下的购物车，什么都没有，直接将购物车从购物车列表里移除
                if (cart.getOrderItemList().size()<1){
                    cartList.remove(cart);
                }
            }

        }

        return cartList;
    }




    /**
     *  根据itemId查询购物车明细列表中是否存在该商品
     * @param orderItemList
     * @param itemId
     * @return
     */
    public TbOrderItem findOrderItemFromList(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().equals(itemId)) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据用户名从redis中查询购物车
     *
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        // 如果是空购物车
        if (cartList==null) {
            //防止是null值添加商品崩溃
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 根据用户名存储购物车数据到Redis
     *
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 登录后合并购物车
     *
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        //先循环购物车
        for (Cart cart : cartList1) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //循环cartList1中每一个购物车的商品
            for (TbOrderItem orderItem : orderItemList) {
                cartList2 = addGoodsToCartList(cartList2,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList2;
    }

    /**
     * 根据key删除购物车
     *
     * @param key
     */
    @Override
    public void deleCartListByUserId(String key) {
        redisTemplate.boundHashOps("cartList").delete(key);
    }


    /**
     * 根据sellerId查询该商品是否已经存在在该商家的购物车
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    public Cart findCartFromCartList(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }

        return null;
    }

    /**
     *  将SKU对象转换成订单对象
     *
     * @param item
     * @param num
     * @return
     */
    public TbOrderItem itemToOrderItem(TbItem item,Integer num){
        TbOrderItem orderItem = new TbOrderItem();

        orderItem.setGoodsId(item.getGoodsId());//商品id
        orderItem.setItemId(item.getId());//SKUid
        orderItem.setNum(num);//数量
        orderItem.setPicPath(item.getImage());//唯一的一张图片
        orderItem.setPrice(item.getPrice());
        orderItem.setTotalFee(new BigDecimal(num * item.getPrice().doubleValue()));//总金额 数量*单价
        orderItem.setTitle(item.getTitle());

        return orderItem;
    }
}





















