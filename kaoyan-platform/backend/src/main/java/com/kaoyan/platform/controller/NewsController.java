package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.News;
import com.kaoyan.platform.entity.Notification;
import com.kaoyan.platform.service.NewsService;
import com.kaoyan.platform.service.NotificationService;
import com.kaoyan.platform.service.SubscriptionService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    
    private final NewsService newsService;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;
    
    @GetMapping("/list")
    public Result<PageResult<News>> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String examStage,
            @RequestParam(required = false) String keyword) {
        return Result.success(newsService.getNewsList(page, size, category, examStage, keyword));
    }
    
    @GetMapping("/detail/{id}")
    public Result<News> getNewsDetail(@PathVariable Long id) {
        newsService.incrementViewCount(id);
        return Result.success(newsService.getNewsById(id));
    }
    
    @PostMapping("/like/{id}")
    public Result<Boolean> likeNews(@PathVariable Long id) {
        newsService.incrementLikeCount(id);
        return Result.success(true);
    }
    
    @PostMapping("/subscribe/{category}")
    public Result<Boolean> subscribe(@PathVariable String category) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        subscriptionService.subscribe(userId, category);
        return Result.success(true);
    }
    
    @PostMapping("/unsubscribe/{category}")
    public Result<Boolean> unsubscribe(@PathVariable String category) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        subscriptionService.unsubscribe(userId, category);
        return Result.success(true);
    }
    
    @GetMapping("/subscriptions")
    public Result<List<String>> getSubscriptions() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return Result.success(subscriptionService.getUserSubscriptions(userId));
    }
    
    @GetMapping("/is-subscribed/{category}")
    public Result<Boolean> isSubscribed(@PathVariable String category) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.success(false);
        }
        return Result.success(subscriptionService.isSubscribed(userId, category));
    }
    
    @GetMapping("/readCount/{id}")
    public Result<Integer> getReadCount(@PathVariable Long id) {
        return Result.success(newsService.getReadCount(id));
    }
}
