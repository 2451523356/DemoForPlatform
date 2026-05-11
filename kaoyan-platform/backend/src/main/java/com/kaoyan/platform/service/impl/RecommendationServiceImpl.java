package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan.platform.entity.News;
import com.kaoyan.platform.entity.Resource;
import com.kaoyan.platform.entity.UserBehaviorLog;
import com.kaoyan.platform.service.NewsService;
import com.kaoyan.platform.service.RecommendationService;
import com.kaoyan.platform.service.ResourceService;
import com.kaoyan.platform.service.UserBehaviorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserBehaviorLogService userBehaviorLogService;
    private final NewsService newsService;
    private final ResourceService resourceService;

    private static final int BEHAVIOR_LOG_LIMIT = 50; // 用于分析用户兴趣的行为日志数量
    private static final int FALLBACK_NEWS_LIMIT = 10; // 默认推荐资讯数量
    private static final int FALLBACK_RESOURCE_LIMIT = 10; // 默认推荐资源数量

    @Override
    public List<News> getRecommendedNews(Long userId, int limit) {
        // 1. 获取用户最近的行为日志
        LambdaQueryWrapper<UserBehaviorLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(UserBehaviorLog::getUserId, userId)
                .in(UserBehaviorLog::getActionType, "view_news", "like_news", "subscribe_category")
                .orderByDesc(UserBehaviorLog::getCreateTime)
                .last("LIMIT " + BEHAVIOR_LOG_LIMIT);
        List<UserBehaviorLog> userBehaviorLogs = userBehaviorLogService.list(logWrapper);

        Set<String> interestedCategories = new HashSet<>();
        Set<Long> interactedNewsIds = new HashSet<>();

        for (UserBehaviorLog log : userBehaviorLogs) {
            if ("view_news".equals(log.getActionType()) || "like_news".equals(log.getActionType())) {
                interactedNewsIds.add(log.getTargetId());
                News news = newsService.getById(log.getTargetId());
                if (news != null && news.getCategory() != null) {
                    interestedCategories.add(news.getCategory());
                }
            } else if ("subscribe_category".equals(log.getActionType()) && log.getTargetType().equals("category")) {
                // 假设 targetId 存储的是 category 的 ID，如果 category 是字符串，这里需要调整
                // 目前 targetId 为 null，所以这里需要从 category 字段获取
                // 由于 UserBehaviorLog 实体中没有 category 字段，这里暂时无法直接获取订阅的分类
                // 假设我们能从 targetId 获取到 category，或者直接从 SubscriptionService 获取
                // 暂时先跳过，后续如果需要，可以优化 UserBehaviorLog 实体或通过 SubscriptionService 获取
            }
        }

        // 2. 基于兴趣类别推荐资讯
        List<News> recommendedNews = new ArrayList<>();
        if (!interestedCategories.isEmpty()) {
            LambdaQueryWrapper<News> newsWrapper = new LambdaQueryWrapper<>();
            newsWrapper.eq(News::getStatus, 1)
                    .in(News::getCategory, interestedCategories)
                    .notIn(!interactedNewsIds.isEmpty(), News::getId, interactedNewsIds) // 排除已交互的资讯
                    .orderByDesc(News::getCreateTime)
                    .last("LIMIT " + limit * 2); // 多取一些，以便后续过滤
            recommendedNews.addAll(newsService.list(newsWrapper));
        }

        // 3. 如果推荐结果不足，补充热门或最新资讯
        if (recommendedNews.size() < limit) {
            LambdaQueryWrapper<News> fallbackNewsWrapper = new LambdaQueryWrapper<>();
            fallbackNewsWrapper.eq(News::getStatus, 1)
                    .notIn(!interactedNewsIds.isEmpty(), News::getId, interactedNewsIds)
                    .orderByDesc(News::getViewCount, News::getCreateTime) // 优先热门，其次最新
                    .last("LIMIT " + (limit - recommendedNews.size()));
            recommendedNews.addAll(newsService.list(fallbackNewsWrapper));
        }

        // 去重并截取到指定数量
        return recommendedNews.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<Resource> getRecommendedResources(Long userId, int limit) {
        // 1. 获取用户最近的行为日志
        LambdaQueryWrapper<UserBehaviorLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(UserBehaviorLog::getUserId, userId)
                .in(UserBehaviorLog::getActionType, "view_resource", "download_resource", "subscribe_category")
                .orderByDesc(UserBehaviorLog::getCreateTime)
                .last("LIMIT " + BEHAVIOR_LOG_LIMIT);
        List<UserBehaviorLog> userBehaviorLogs = userBehaviorLogService.list(logWrapper);

        Set<String> interestedCategories = new HashSet<>();
        Set<String> interestedSubjects = new HashSet<>();
        Set<Long> interactedResourceIds = new HashSet<>();

        for (UserBehaviorLog log : userBehaviorLogs) {
            if ("view_resource".equals(log.getActionType()) || "download_resource".equals(log.getActionType())) {
                interactedResourceIds.add(log.getTargetId());
                Resource resource = resourceService.getById(log.getTargetId());
                if (resource != null) {
                    if (resource.getCategory() != null) {
                        interestedCategories.add(resource.getCategory());
                    }
                    if (resource.getSubject() != null) {
                        interestedSubjects.add(resource.getSubject());
                    }
                }
            } else if ("subscribe_category".equals(log.getActionType()) && log.getTargetType().equals("category")) {
                // 同上，暂时无法直接获取订阅的分类，后续优化
            }
        }

        // 2. 基于兴趣类别和学科推荐资源
        List<Resource> recommendedResources = new ArrayList<>();
        if (!interestedCategories.isEmpty() || !interestedSubjects.isEmpty()) {
            LambdaQueryWrapper<Resource> resourceWrapper = new LambdaQueryWrapper<>();
            resourceWrapper.eq(Resource::getStatus, 1)
                    .and(w -> {
                        if (!interestedCategories.isEmpty()) {
                            w.in(Resource::getCategory, interestedCategories);
                        }
                        if (!interestedSubjects.isEmpty()) {
                            w.or().in(Resource::getSubject, interestedSubjects);
                        }
                    })
                    .notIn(!interactedResourceIds.isEmpty(), Resource::getId, interactedResourceIds) // 排除已交互的资源
                    .orderByDesc(Resource::getCreateTime)
                    .last("LIMIT " + limit * 2);
            recommendedResources.addAll(resourceService.list(resourceWrapper));
        }

        // 3. 如果推荐结果不足，补充热门或最新资源
        if (recommendedResources.size() < limit) {
            LambdaQueryWrapper<Resource> fallbackResourceWrapper = new LambdaQueryWrapper<>();
            fallbackResourceWrapper.eq(Resource::getStatus, 1)
                    .notIn(!interactedResourceIds.isEmpty(), Resource::getId, interactedResourceIds)
                    .orderByDesc(Resource::getDownloadCount, Resource::getViewCount, Resource::getCreateTime) // 优先热门，其次最新
                    .last("LIMIT " + (limit - recommendedResources.size()));
            recommendedResources.addAll(resourceService.list(fallbackResourceWrapper));
        }

        // 去重并截取到指定数量
        return recommendedResources.stream().distinct().limit(limit).collect(Collectors.toList());
    }
}
