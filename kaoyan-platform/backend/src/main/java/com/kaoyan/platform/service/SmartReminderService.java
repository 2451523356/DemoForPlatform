package com.kaoyan.platform.service;

import com.kaoyan.platform.entity.User;

public interface SmartReminderService {

    void sendMiniProgramReminder(Long userId, String templateId, String title, String content, String pagePath);

    void sendSmsReminder(Long userId, String message);

    void sendStudyReminder(Long userId);

    void sendTaskReminder(Long userId, Long taskId, String taskTitle);

    void sendResourceUpdateReminder(Long userId, Long resourceId, String resourceTitle);

    void sendCourseUpdateReminder(Long userId, Long courseId, String courseTitle);
}
