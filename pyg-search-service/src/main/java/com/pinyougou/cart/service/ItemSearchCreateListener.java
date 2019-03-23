package com.pinyougou.cart.service;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @Auther: Gorx
 * @Date: 2019/3/18 22:02
 * @Description:
 */
@Component
public class ItemSearchCreateListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            itemSearchService.importItemToSolr(ids);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}





















