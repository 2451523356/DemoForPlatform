package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.service.SmartReminderService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final SmartReminderService smartReminderService;

    @PostMapping("/study")
    public Result<Void> sendStudyReminder() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        smartReminderService.sendStudyReminder(userId);
        return Result.success();
    }

    @PostMapping("/task")
    public Result<Void> sendTaskReminder(
            @RequestParam Long taskId,
            @RequestParam String taskTitle) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        smartReminderService.sendTaskReminder(userId, taskId, taskTitle);
        return Result.success();
    }

    @PostMapping("/resource-update")
    public Result<Void> sendResourceUpdateReminder(
            @RequestParam Long resourceId,
            @RequestParam String resourceTitle) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        smartReminderService.sendResourceUpdateReminder(userId, resourceId, resourceTitle);
        return Result.success();
    }

    @PostMapping("/course-update")
    public Result<Void> sendCourseUpdateReminder(
            @RequestParam Long courseId,
            @RequestParam String courseTitle) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        smartReminderService.sendCourseUpdateReminder(userId, courseId, courseTitle);
        return Result.success();
    }

    @PostMapping("/sms")
    public Result<Void> sendSmsReminder(@RequestParam String message) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        smartReminderService.sendSmsReminder(userId, message);
        return Result.success();
    }
}
