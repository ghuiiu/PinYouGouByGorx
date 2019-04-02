package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.Map;

/**
 * @Auther: Gorx
 * @Date: 2019/3/26 09:36
 * @Description:
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        IdWorker idWorker = new IdWorker();
        //从安全认证框架获取登录用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = payService.searchPayLogFromRedis(name);
        if (payLog!=null) {
            //两参数，1.支付单号  2支付金额 是分
            System.out.println("=支付金额是：="+payLog.getTotalFee());
            return payService.createNative(idWorker.nextId()+"","1");
        }
        return null;
    }

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //为了计次
        int timer = 1;

        while (true){
            Map map = payService.queryPayStatus(out_trade_no);

            //支付失败
            if(map == null){
                return new Result(false, "支付失败");
            }

            //支付成功
            if("SUCCESS".equals(map.get("trade_state"))){
                //修改支付单状态&修改订单状态,参数1，支付单id，参数2，
                payService.updateOrderStatus(out_trade_no,map.get("transaction_id").toString());

                return new Result(true, "支付成功");
            }

            //为了超时的时候，退出循环查询支付状态
            if(timer > 6){
                return new Result(false, "timeout");
            }
            timer++;

            //5秒一查询
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
































