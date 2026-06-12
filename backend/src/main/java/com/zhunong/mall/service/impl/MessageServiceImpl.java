package com.zhunong.mall.service.impl;

import com.zhunong.mall.entity.Message;
import com.zhunong.mall.mapper.MessageMapper;
import com.zhunong.mall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void sendMessage(Long userId, String title, String content) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(1);
        message.setIsRead(0);
        messageMapper.insert(message);
    }

    @Override
    public void sendAuditMessage(Long userId, Long relatedId, String title, String content) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(2);
        message.setRelatedId(relatedId);
        message.setRelatedType("product_audit");
        message.setIsRead(0);
        messageMapper.insert(message);
    }
}
