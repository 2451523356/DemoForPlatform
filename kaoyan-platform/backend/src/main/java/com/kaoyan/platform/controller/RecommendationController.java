package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.News;
import com.kaoyan.platform.entity.Resource;
import com.kaoyan.platform.service.RecommendationService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/news")
    public Result<List<News>> getRecommendedNews(@RequestParam(defaultValue = "5") int limit) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            // 未登录用户，可以返回热门或最新资讯
            return Result.success(recommendationService.getRecommendedNews(null, limit));
        }
        return Result.success(recommendationService.getRecommendedNews(userId, limit));
    }

    @GetMapping("/resources")
    public Result<List<Resource>> getRecommendedResources(@RequestParam(defaultValue = "5") int limit) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            // 未登录用户，可以返回热门或最新资源
            return Result.success(recommendationService.getRecommendedResources(null, limit));
        }
        return Result.success(recommendationService.getRecommendedResources(userId, limit));
    }
}
