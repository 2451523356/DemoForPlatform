package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Notification;

public interface NotificationService extends IService<Notification> {
    
    PageResult<Notification> getNotifications(Long userId, Integer page, Integer size);
    
    void markAsRead(Long userId, Long notificationId);
    
    void markAllAsRead(Long userId);
    
    Integer getUnreadCount(Long userId);
    
    void createNotification(Long userId, String title, String content, String type, Long relatedId);
}
