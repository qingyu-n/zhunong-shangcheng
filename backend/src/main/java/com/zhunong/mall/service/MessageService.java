package com.zhunong.mall.service;

public interface MessageService {

    void sendMessage(Long userId, String title, String content);

    void sendAuditMessage(Long userId, Long relatedId, String title, String content);
}
