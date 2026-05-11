package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.News;
import com.kaoyan.platform.mapper.NewsMapper;
import com.kaoyan.platform.service.NewsService;
import com.kaoyan.platform.service.NotificationPushService;
import com.kaoyan.platform.service.UserBehaviorLogService;
import com.kaoyan.platform.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Autowired
    private UserBehaviorLogService userBehaviorLogService;

    @Autowired
    private NotificationPushService notificationPushService;

    @Override
    public PageResult<News> getNewsList(Integer page, Integer size, String category, String examStage, String keyword) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, 1);

        if (category != null && !category.isEmpty()) {
            if (category.contains(",")) {
                String[] categories = category.split(",");
                wrapper.in(News::getCategory, (Object[]) categories);
            } else {
                wrapper.eq(News::getCategory, category);
            }
        }

        if (examStage != null && !examStage.isEmpty()) {
            wrapper.eq(News::getExamStage, examStage);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(News::getTitle, keyword).or().like(News::getContent, keyword));
        }

        wrapper.orderByDesc(News::getIsTop).orderByDesc(News::getCreateTime);

        Page<News> newsPage = new Page<>(page, size);
        newsPage = baseMapper.selectPage(newsPage, wrapper);

        return new PageResult<>(newsPage.getTotal(), (long) page, (long) size, newsPage.getRecords());
    }

    @Override
    public PageResult<News> getAdminNewsList(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(News::getTitle, keyword).or().like(News::getContent, keyword));
        }

        wrapper.orderByDesc(News::getId);

        Page<News> newsPage = new Page<>(page, size);
        newsPage = baseMapper.selectPage(newsPage, wrapper);

        return new PageResult<>(newsPage.getTotal(), (long) page, (long) size, newsPage.getRecords());
    }

    @Override
    public News getNewsById(Long id) {
        News news = baseMapper.selectById(id);
        if (news != null) {
            // 记录用户查看资讯的行为
            Long userId = UserContext.getUserId();
            if (userId != null) {
                recordNewsView(userId, id);
            }
        }
        return news;
    }

    @Override
    public void incrementViewCount(Long id) {
        News news = baseMapper.selectById(id);
        if (news != null) {
            news.setViewCount(news.getViewCount() + 1);
            baseMapper.updateById(news);
        }
    }

    @Override
    public void incrementLikeCount(Long id) {
        News news = baseMapper.selectById(id);
        if (news != null) {
            news.setLikeCount(news.getLikeCount() + 1);
            baseMapper.updateById(news);
            // 记录用户点赞资讯的行为
            Long userId = UserContext.getUserId();
            if (userId != null) {
                recordNewsLike(userId, id);
            }
        }
    }

    @Override
    public Integer getReadCount(Long id) {
        News news = baseMapper.selectById(id);
        return news != null ? news.getViewCount() : 0;
    }

    @Override
    public boolean removeById(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public News getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean save(News news) {
        boolean saved = super.save(news);
        if (saved && news.getStatus() != null && news.getStatus() == 1 && news.getCategory() != null) {
            notificationPushService.pushNewsUpdateNotification(
                    news.getId(),
                    news.getTitle(),
                    news.getCategory()
            );
        }
        return saved;
    }

    @Override
    public boolean updateById(News news) {
        boolean updated = super.updateById(news);
        if (updated && news.getStatus() != null && news.getStatus() == 1 && news.getCategory() != null) {
            notificationPushService.pushNewsUpdateNotification(
                    news.getId(),
                    news.getTitle(),
                    news.getCategory()
            );
        }
        return updated;
    }

    @Override
    public void recordNewsView(Long userId, Long newsId) {
        userBehaviorLogService.recordBehavior(userId, "view_news", "news", newsId);
    }

    @Override
    public void recordNewsLike(Long userId, Long newsId) {
        userBehaviorLogService.recordBehavior(userId, "like_news", "news", newsId);
    }
}
