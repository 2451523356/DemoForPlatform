package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.Notification;
import com.kaoyan.platform.service.NotificationService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping("/list")
    public Result<PageResult<Notification>> getNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return Result.success(notificationService.getNotifications(userId, page, size));
    }
    
    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        notificationService.markAsRead(userId, id);
        return Result.success();
    }
    
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.success(0);
        }
        return Result.success(notificationService.getUnreadCount(userId));
    }
}
