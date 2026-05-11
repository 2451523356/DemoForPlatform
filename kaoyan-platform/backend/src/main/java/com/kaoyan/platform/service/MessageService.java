package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService extends IService<Message> {
    
    void sendMessage(Long fromUserId, Long toUserId, String content);
    
    PageResult<Message> getMessageList(Long userId, Long otherUserId, Integer page, Integer size);
    
    void markAsRead(Long messageId);
    
    Integer getUnreadCount(Long userId);
    
    List<Long> getRecentContacts(Long userId, Integer limit);
    
    List<Map<String, Object>> getRecentContactsWithInfo(Long userId, Integer limit);
    
    void markAllAsRead(Long userId, Long fromUserId);
}