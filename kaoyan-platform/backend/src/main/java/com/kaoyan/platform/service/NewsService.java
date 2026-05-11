package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.News;

public interface NewsService extends IService<News> {

    PageResult<News> getNewsList(Integer page, Integer size, String category, String examStage, String keyword);

    PageResult<News> getAdminNewsList(Integer page, Integer size, String keyword);

    News getNewsById(Long id);

    void incrementViewCount(Long id);

    void incrementLikeCount(Long id);

    Integer getReadCount(Long id);

    boolean removeById(Long id);

    News getById(Long id);

    void recordNewsView(Long userId, Long newsId);

    void recordNewsLike(Long userId, Long newsId);
}
