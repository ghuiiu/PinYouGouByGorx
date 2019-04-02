package com.pinyougou.pay.service;

import com.pinyougou.pojo.TbPayLog;

import java.util.Map;

/**
 * @Auther: Gorx
 * @Date: 2019/3/24 21:07
 * @Description:
 */
public interface PayService {

    /**
     *  生成微信支付二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    Map createNative (String out_trade_no, String total_fee);


    /**
     * 查询订单状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    public TbPayLog searchPayLogFromRedis(String key);


    public void updateOrderStatus(String out_trade_no, String transaction_id);

}
