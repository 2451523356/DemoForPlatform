package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.service.SubscriptionService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @GetMapping("/list")
    public Result<List<String>> getSubscriptions() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        List<String> subscriptions = subscriptionService.getUserSubscriptions(userId);
        return Result.success(subscriptions);
    }
    
    @PostMapping
    public Result<Void> saveSubscriptions(@RequestBody List<String> categories) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 先清除用户的所有订阅
        List<String> existingSubscriptions = subscriptionService.getUserSubscriptions(userId);
        for (String category : existingSubscriptions) {
            subscriptionService.unsubscribe(userId, category);
        }
        
        // 添加新的订阅
        for (String category : categories) {
            subscriptionService.subscribe(userId, category);
        }
        
        return Result.success();
    }
}