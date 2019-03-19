package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @Auther: Gorx
 * @Date: 2019/3/19 17:25
 * @Description:
 */
@Component
public class ItemPageCreateListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            for (Long id : ids) {
                itemPageService.createHtml(id);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}















