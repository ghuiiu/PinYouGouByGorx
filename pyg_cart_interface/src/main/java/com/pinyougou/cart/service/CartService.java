package com.pinyougou.cart.service;

import entity.Cart;

import java.util.List;

/**
 * @Auther: Gorx
 * @Date: 2019/3/21 09:54
 * @Description:
 */
public interface CartService {

    /**
     * 根据商品信息把商品添加到购物车集合中
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);


    /**
     *
     *  登录后合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    List<Cart> mergeCartList( List<Cart> cartList1, List<Cart> cartList2);

    /**
     * 根据key删除购物车
     * @param key
     */
    void deleCartListByUserId(String key);
}









