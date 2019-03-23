package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.CartService;
import entity.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

/**
 * @Auther: Gorx
 * @Date: 2019/3/21 10:02
 * @Description:
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 将商品添加购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        try {
            String uuid = getUuid();
            String loginName = SecurityContextHolder.getContext().getAuthentication().getName();

            //判断用户如果已经登录
            if (!"anonymousUser".equals(loginName)){
                //uuid变成登录后的用户名id
                uuid = loginName;
            }
            //从redis中获取购物车
            List<Cart> cartList = cartService.findCartListFromRedis(uuid);
            //商品添加购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //将添加好商品的购物车存到redis中
            cartService.saveCartListToRedis(uuid,cartList);

            return new Result(true,"添加购物车成功!!!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败!!!");
        }
    }


    //从cookie中获取唯一的key值
    public String getUuid(){
        String uuid = CookieUtil.getCookieValue(request, "uuid", "utf-8");
        //如果cookie中没有任何内容
        if (uuid == null || uuid.equals("")){
            uuid = UUID.randomUUID().toString();
            //存cookie中
            CookieUtil.setCookie(request,response,"uuid",uuid,48*60*60,"utf-8");
        }
        return uuid;
    }

    //根据用户名从redis中查询购物车
    @RequestMapping("/findCartListFromRedis")
    public List<Cart> findCartListFromRedis(){
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        //获取唯一的key值
        String uuid = getUuid();
        //从redis取购物车取未登录的购物车
        List<Cart> cartList = cartService.findCartListFromRedis(uuid);
        //判断用户如果已经登录
        if (!"anonymousUser".equals(loginName)){
            //在第一次登录的时候合并购物车,登录后的购物车
            List<Cart> listFromRedis = cartService.findCartListFromRedis(loginName);

            //当未登录的购物车的size>0的时候，才合并
            if (cartList.size()>0) {
                //1.合并购物车
                cartList = cartService.mergeCartList(cartList,listFromRedis);
                //2.将合并的购物车保存redis
                cartService.saveCartListToRedis(loginName,cartList);
                //3.清除未登录的购物车
                cartService.deleCartListByUserId(uuid);
            }else {
                //如果未登录购物车已经空了，直接将登录购物车返回
                cartList = listFromRedis;
            }
        }
        return cartList;
    }
}























