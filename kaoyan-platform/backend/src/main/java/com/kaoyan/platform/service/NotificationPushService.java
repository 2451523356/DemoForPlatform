package com.kaoyan.platform.service;

public interface NotificationPushService {

    void pushResourceUpdateNotification(Long resourceId, String resourceTitle, String category);

    void pushNewsUpdateNotification(Long newsId, String newsTitle, String category);

    void pushCourseUpdateNotification(Long courseId, String courseTitle, String category);

    void pushSystemNotification(String title, String content);

    void pushDeadlineReminder(Long userId, Long todoId, String todoTitle, boolean isOverdue);
}
