package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import util.HttpClientUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: Gorx
 * @Date: 2019/3/24 21:09
 * @Description:
 */
@Service
public class PayServiceImpl implements PayService {

    @Value("${appid}")
    private String appid; //公众账号ID

    @Value("${partner}")
    private String mch_id;  //商家号

    @Value("${notifyurl}")
    private String notifyurl;  //通知地址

    @Value("${partnerkey}")
    private String partnerkey;   //签名

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbPayLogMapper payLogMapper;

    @Autowired
    private TbOrderMapper orderMapper;



    /**
     * 生成微信支付二维码
     *
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        try {
            //封装请求参数
            Map requestMap = new HashMap();

            requestMap.put("appid",appid);
            requestMap.put("mch_id",mch_id);
            requestMap.put("nonce_str",WXPayUtil.generateUUID());
            requestMap.put("body","nmsl潮牌");
            //支付单号
            requestMap.put("out_trade_no",out_trade_no);
            requestMap.put("total_fee",total_fee);
            requestMap.put("spbill_create_ip", "127.0.0.1");
            //通知地址,支付方式二并不太依赖
            requestMap.put("notify_url",notifyurl);
            //交易类型
            requestMap.put("trade_type", "NATIVE ");

            //将map转成xml格式并且还得签名
            String requestStr = WXPayUtil.generateSignedXml(requestMap, partnerkey);
            System.out.println("=发送的内容="+requestStr);

            //发http请求，拿到http工具类
            HttpClientUtil httpClient = new HttpClientUtil("https://api.mch.weixin.qq.com/pay/unifiedorder");

            //准备发送的参数(需要string类型的xml字符串)
            httpClient.setXmlParam(requestStr);

            //需要设置https的协议
            httpClient.setHttps(true);

            //发送post请求
            httpClient.post();

            //接收string类型xml格式返回值
            String content = httpClient.getContent();
            System.out.println("微信返回的="+content);
            //通过工具类将string的xml转成map
            Map<String, String> map = WXPayUtil.xmlToMap(content);

            //map提取想要的属性code_url,支付地址，还需要支付单号和支付总金额
            Map responseMap = new HashMap();
            //支付地址
            responseMap.put("code_url",map.get("code_url"));
            //支付单号
            responseMap.put("out_trade_no",out_trade_no);
            //支付总金额
            responseMap.put("total_fee",total_fee);
            return responseMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }

    /**
     * 查询订单状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //封装请求参数
            Map resultMap = new HashMap();
            resultMap.put("appid",appid);
            resultMap.put("mch_id",mch_id);
            //支付单号
            resultMap.put("out_trade_no",out_trade_no);
            resultMap.put("nonce_str",WXPayUtil.generateUUID());
            //将map转成xml格式并且还得签名，参数一是请求的map要转成xml格式，参数2带上的签名，必须带
            String requestStr = WXPayUtil.generateSignedXml(resultMap, partnerkey);

            //发http请求，拿到http工具类
            HttpClientUtil httpClient = new HttpClientUtil("https://api.mch.weixin.qq.com/pay/orderquery");

            //准备发送的参数(需要string类型的xml字符串)
            httpClient.setXmlParam(requestStr);

            //需要设置https的协议
            httpClient.setHttps(true);

            //发送post请求
            httpClient.post();

            //接收string类型xml格式返回值
            String content = httpClient.getContent();

            System.out.println("微信返回的="+content);
            //通过工具类将string的xml转成map
            Map<String, String> map = WXPayUtil.xmlToMap(content);

            //直接将map返回给contorller进行判断即可
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 根据Redis查询支付日志
     * @param key
     * @return
     */
    @Override
    public TbPayLog searchPayLogFromRedis(String key) {
        return (TbPayLog)redisTemplate.boundHashOps("payLog").get(key);
    }

    /**
     *
     * @param out_trade_no
     * @param transaction_id
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //根据支付单号获取支付单对象
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        //付款时间
        payLog.setPayTime(new Date());
        //0未付款  1已付款
        payLog.setTradeState("1");
        //微信返回的业务代码
        payLog.setTransactionId(transaction_id);
        //更新数据库
        payLogMapper.updateByPrimaryKey(payLog);

        String[] orderIds = payLog.getOrderList().split(",");

        //循环支付单对应的所有订购的id，获取对象，修改状态和时间
        for (String orderId : orderIds) {
            //获取订单对象
            TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            //订单的支付时间
            order.setPaymentTime(new Date());
            //'状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
            order.setStatus("2");
            orderMapper.updateByPrimaryKey(order);
        }

        //清理redis缓存,清理支付单对象
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}














