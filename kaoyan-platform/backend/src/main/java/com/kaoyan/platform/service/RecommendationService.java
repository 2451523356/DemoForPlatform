package com.kaoyan.platform.service;

import com.kaoyan.platform.entity.News;
import com.kaoyan.platform.entity.Resource;

import java.util.List;

public interface RecommendationService {
    List<News> getRecommendedNews(Long userId, int limit);
    List<Resource> getRecommendedResources(Long userId, int limit);
}
