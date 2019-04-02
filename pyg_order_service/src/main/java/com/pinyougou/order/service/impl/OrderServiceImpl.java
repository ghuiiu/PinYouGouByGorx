package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
    private RedisTemplate redisTemplate;

	@Autowired
    private IdWorker idWorker;

	@Autowired
    private TbOrderItemMapper orderItemMapper;

	@Autowired
    private TbPayLogMapper payLogMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
	    //首选从redis中根据订单的userId取出购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        //循环购物车将每个Cart赋值给单独TbOrder(12)，雪花算法生成OrderID

        double payMoney = 0.0;
        //保存订单id的集合
        List orderIds = new ArrayList<>();

        for (Cart cart : cartList) {
            double totalMoney = 0.0;

            TbOrder dbOrder = new TbOrder();
            //保存谁下的订单
            dbOrder.setUserId(order.getUserId());
            //'订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
            dbOrder.setSourceType("2");
            //收货人电话
            dbOrder.setReceiverMobile(order.getReceiverMobile());
            //收货人
            dbOrder.setReceiver(order.getReceiver());
            //收货地址
            dbOrder.setReceiverAreaName(order.getReceiverAreaName());
            //订单创建时间
            dbOrder.setCreateTime(new Date());
            //支付方式
            dbOrder.setPaymentType(order.getPaymentType());
            //订单更新时间
            dbOrder.setUpdateTime(new Date());

            //利用雪花算法完成id生成
            long orderId = idWorker.nextId();
            dbOrder.setOrderId(orderId);
            //将订单id放入集合
            orderIds.add(orderId);
            //订单明细

            //购物车明细
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                //商品金额累计
                totalMoney += orderItem.getTotalFee().doubleValue();
                //设置id，以及多对一关系即可
                orderItem.setId(idWorker.nextId());
                //多对一关系
                orderItem.setOrderId(orderId);
                orderItemMapper.insert(orderItem);
            }
            //循环购物车中所有的明细求总金额

            //支付总金额是当前订单的总金额
            dbOrder.setPayment(new BigDecimal(totalMoney));
            //商家id在购物车上
            dbOrder.setSellerId(cart.getSellerId());
            // '状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
            dbOrder.setStatus("1");

            //累加每张订单的总金额，就是支付单总金额
            payMoney += totalMoney;
            //订单保存到数据库
            orderMapper.insert(dbOrder);

        }
        //清空该用户的购物车
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

        //生产支付单对象，并且保存数据库
        TbPayLog payLog = new TbPayLog();
        //支付单号采用雪花算法
        payLog.setOutTradeNo(idWorker.nextId()+"");
        //支付日期
        payLog.setCreateTime(new Date());
        //这里是分
        payLog.setTotalFee((long)(payMoney*100));
        //哪个用户的支付单
        payLog.setUserId(order.getUserId());
        //0未支付  1已支付
        payLog.setTradeState("0");

        //[xxxx ,xxxx ,xxxx, xxxxx] 去掉左右括号，去掉，号中间的空格
        payLog.setOrderList(orderIds.toString().replace("[","").replace("]","").replaceAll(" ",""));
        //1，微信支付  2.货到付款
        payLog.setPayType("1");
        //将未支付的支付单保存数据库
        payLogMapper.insert(payLog);

        //将payLog放入redis
        redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
    }


}






















