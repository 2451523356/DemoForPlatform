package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Notification;
import com.kaoyan.platform.mapper.NotificationMapper;
import com.kaoyan.platform.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    @Override
    public PageResult<Notification> getNotifications(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.orderByDesc(Notification::getCreateTime);
        
        Page<Notification> notificationPage = page(new Page<>(page, size), wrapper);
        
        return new PageResult<>(notificationPage.getTotal(), (long) page, (long) size, notificationPage.getRecords());
    }
    
    @Override
    public void markAsRead(Long userId, Long notificationId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, notificationId);
        wrapper.eq(Notification::getUserId, userId);
        Notification notification = getOne(wrapper);
        
        if (notification != null) {
            notification.setIsRead(1);
            updateById(notification);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(Notification::getIsRead, 0);
        
        Notification notification = new Notification();
        notification.setIsRead(1);
        update(notification, wrapper);
    }
    
    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(Notification::getIsRead, 0);
        return Math.toIntExact(count(wrapper));
    }
    
    @Override
    public void createNotification(Long userId, String title, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        save(notification);
    }
}
