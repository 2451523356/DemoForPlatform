package com.kaoyan.platform.service.impl;

import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.service.NotificationService;
import com.kaoyan.platform.service.SmartReminderService;
import com.kaoyan.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartReminderServiceImpl implements SmartReminderService {

    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    public void sendMiniProgramReminder(Long userId, String templateId, String title, String content, String pagePath) {
        User user = userService.getById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return;
        }
        // 创建站内通知（小程序消息需要微信API，此处创建站内通知作为替代）
        notificationService.createNotification(userId, title, content, "system", null);
    }

    @Override
    public void sendSmsReminder(Long userId, String message) {
        User user = userService.getById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return;
        }
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            log.warn("User {} has no phone number, creating in-app notification instead", userId);
            notificationService.createNotification(userId, "系统提醒", message, "system", null);
            return;
        }
        // SMS sent via external service; fallback to in-app notification
        notificationService.createNotification(userId, "系统提醒", message, "system", null);
    }

    @Override
    public void sendStudyReminder(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return;
        }
        String title = "学习提醒";
        String message = "亲爱的" + (user.getNickname() != null ? user.getNickname() : "同学")
                + ", 今天的学习计划完成了吗？坚持学习，加油！";
        notificationService.createNotification(userId, title, message, "system", null);
        log.info("Study reminder sent to user {}", userId);
    }

    @Override
    public void sendTaskReminder(Long userId, Long taskId, String taskTitle) {
        User user = userService.getById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return;
        }
        String title = "任务提醒";
        String message = "亲爱的" + (user.getNickname() != null ? user.getNickname() : "同学")
                + ", 您的任务\"" + taskTitle + "\"即将到期，请注意完成！";
        notificationService.createNotification(userId, title, message, "system", taskId);
        log.info("Task reminder sent to user {} for task {}", userId, taskId);
    }

    @Override
    public void sendResourceUpdateReminder(Long userId, Long resourceId, String resourceTitle) {
        User user = userService.getById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return;
        }
        String title = "资源更新提醒";
        String message = "亲爱的" + (user.getNickname() != null ? user.getNickname() : "同学")
                + ", 您关注的资源\"" + resourceTitle + "\"已经更新，快去查看吧！";
        notificationService.createNotification(userId, title, message, "resource", resourceId);
    }

    @Override
    public void sendCourseUpdateReminder(Long userId, Long courseId, String courseTitle) {
        User user = userService.getById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return;
        }
        String title = "课程更新提醒";
        String message = "亲爱的" + (user.getNickname() != null ? user.getNickname() : "同学")
                + ", 您学习的课程\"" + courseTitle + "\"已经更新，快去学习吧！";
        notificationService.createNotification(userId, title, message, "course", courseId);
    }
}
