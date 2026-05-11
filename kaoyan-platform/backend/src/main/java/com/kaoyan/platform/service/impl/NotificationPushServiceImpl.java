package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan.platform.entity.Subscription;
import com.kaoyan.platform.mapper.SubscriptionMapper;
import com.kaoyan.platform.service.NotificationPushService;
import com.kaoyan.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationPushServiceImpl implements NotificationPushService {

    private final SubscriptionMapper subscriptionMapper;
    private final NotificationService notificationService;

    @Override
    public void pushResourceUpdateNotification(Long resourceId, String resourceTitle, String category) {
        List<Long> userIds = getSubscribedUserIds(category);
        for (Long userId : userIds) {
            notificationService.createNotification(
                    userId,
                    "资源更新通知",
                    "您订阅的分类有新资源：" + resourceTitle,
                    "resource_update",
                    resourceId
            );
        }
    }

    @Override
    public void pushNewsUpdateNotification(Long newsId, String newsTitle, String category) {
        List<Long> userIds = getSubscribedUserIds(category);
        for (Long userId : userIds) {
            notificationService.createNotification(
                    userId,
                    "资讯更新通知",
                    "您订阅的分类有新资讯：" + newsTitle,
                    "news_update",
                    newsId
            );
        }
    }

    @Override
    public void pushCourseUpdateNotification(Long courseId, String courseTitle, String category) {
        List<Long> userIds = getSubscribedUserIds(category);
        for (Long userId : userIds) {
            notificationService.createNotification(
                    userId,
                    "课程更新通知",
                    "您订阅的分类有新课程：" + courseTitle,
                    "course_update",
                    courseId
            );
        }
    }

    @Override
    public void pushSystemNotification(String title, String content) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getStatus, 1);
        List<Long> userIds = subscriptionMapper.selectList(wrapper)
                .stream()
                .map(Subscription::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        for (Long userId : userIds) {
            notificationService.createNotification(
                    userId,
                    title,
                    content,
                    "system",
                    null
            );
        }
    }

    @Override
    public void pushDeadlineReminder(Long userId, Long todoId, String todoTitle, boolean isOverdue) {
        if (isOverdue) {
            notificationService.createNotification(
                    userId,
                    "任务已过期",
                    "您的任务\"" + todoTitle + "\"已经超过截止日期，请及时处理！",
                    "deadline",
                    todoId
            );
        } else {
            notificationService.createNotification(
                    userId,
                    "任务即将到期",
                    "您的任务\"" + todoTitle + "\"即将到期，请尽快完成！",
                    "deadline",
                    todoId
            );
        }
    }

    private List<Long> getSubscribedUserIds(String category) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getCategory, category);
        wrapper.eq(Subscription::getStatus, 1);
        return subscriptionMapper.selectList(wrapper)
                .stream()
                .map(Subscription::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }
}
