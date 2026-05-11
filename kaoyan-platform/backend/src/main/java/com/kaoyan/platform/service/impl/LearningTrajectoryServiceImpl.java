package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.*;
import com.kaoyan.platform.mapper.UserBehaviorLogMapper;
import com.kaoyan.platform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningTrajectoryServiceImpl implements LearningTrajectoryService {

    @Autowired
    private StudyRecordService studyRecordService;

    @Autowired
    private DownloadHistoryService downloadHistoryService;

    @Autowired
    private UserBehaviorLogService userBehaviorLogService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private UserBehaviorLogMapper userBehaviorLogMapper;

    @Override
    public PageResult<Map<String, Object>> getLearningTrajectory(Long userId, Integer page, Integer size) {
        // 获取所有相关数据
        List<StudyRecord> studyRecords = studyRecordService.getStudyRecordsByDateRange(userId, LocalDate.now().minusDays(365), LocalDate.now());
        List<DownloadHistory> downloadHistories = downloadHistoryService.getDownloadHistoryByUserId(userId);
        List<UserBehaviorLog> behaviorLogs = userBehaviorLogMapper.selectList(
                new LambdaQueryWrapper<UserBehaviorLog>()
                        .eq(UserBehaviorLog::getUserId, userId)
                        .orderByDesc(UserBehaviorLog::getCreateTime)
        );

        // 整合数据
        List<Map<String, Object>> trajectory = new ArrayList<>();

        // 添加学习记录
        for (StudyRecord record : studyRecords) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "study");
            item.put("content", record.getContent());
            item.put("subject", record.getSubject());
            item.put("duration", record.getDuration());
            item.put("date", record.getStudyDate());
            item.put("time", record.getCreateTime());
            trajectory.add(item);
        }

        // 添加下载记录
        for (DownloadHistory history : downloadHistories) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "download");
            item.put("resourceTitle", history.getResourceTitle());
            item.put("resourceCategory", history.getResourceCategory());
            item.put("pointsConsumed", history.getPointsConsumed());
            item.put("date", history.getDownloadTime().toLocalDate());
            item.put("time", history.getDownloadTime());
            trajectory.add(item);
        }

        // 添加资讯阅读记录
        for (UserBehaviorLog log : behaviorLogs) {
            if ("view_news".equals(log.getActionType()) && log.getTargetId() != null) {
                News news = newsService.getById(log.getTargetId());
                if (news != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "read_news");
                    item.put("newsTitle", news.getTitle());
                    item.put("date", log.getCreateTime().toLocalDate());
                    item.put("time", log.getCreateTime());
                    trajectory.add(item);
                }
            }
        }

        // 按时间倒序排序
        trajectory.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("time");
            LocalDateTime timeB = (LocalDateTime) b.get("time");
            return timeB.compareTo(timeA);
        });

        // 分页处理
        int start = (page - 1) * size;
        int end = Math.min(start + size, trajectory.size());
        List<Map<String, Object>> pageData = trajectory.subList(start, end);

        return new PageResult<>((long) trajectory.size(), (long) page, (long) size, pageData);
    }

    @Override
    public List<Map<String, Object>> getLearningTrajectoryByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取指定日期范围的学习记录
        List<StudyRecord> studyRecords = studyRecordService.getStudyRecordsByDateRange(userId, startDate, endDate);
        
        // 获取下载记录
        List<DownloadHistory> downloadHistories = downloadHistoryService.getDownloadHistoryByUserId(userId);
        List<DownloadHistory> filteredDownloads = downloadHistories.stream()
                .filter(history -> {
                    LocalDate downloadDate = history.getDownloadTime().toLocalDate();
                    return !downloadDate.isBefore(startDate) && !downloadDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // 获取资讯阅读记录
        List<UserBehaviorLog> behaviorLogs = userBehaviorLogMapper.selectList(
                new LambdaQueryWrapper<UserBehaviorLog>()
                        .eq(UserBehaviorLog::getUserId, userId)
                        .eq(UserBehaviorLog::getActionType, "view_news")
                        .ge(UserBehaviorLog::getCreateTime, startDate.atStartOfDay())
                        .le(UserBehaviorLog::getCreateTime, endDate.plusDays(1).atStartOfDay().minusNanos(1))
        );

        // 整合数据
        List<Map<String, Object>> trajectory = new ArrayList<>();

        // 添加学习记录
        for (StudyRecord record : studyRecords) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "study");
            item.put("content", record.getContent());
            item.put("subject", record.getSubject());
            item.put("duration", record.getDuration());
            item.put("date", record.getStudyDate());
            item.put("time", record.getCreateTime());
            trajectory.add(item);
        }

        // 添加下载记录
        for (DownloadHistory history : filteredDownloads) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "download");
            item.put("resourceTitle", history.getResourceTitle());
            item.put("resourceCategory", history.getResourceCategory());
            item.put("pointsConsumed", history.getPointsConsumed());
            item.put("date", history.getDownloadTime().toLocalDate());
            item.put("time", history.getDownloadTime());
            trajectory.add(item);
        }

        // 添加资讯阅读记录
        for (UserBehaviorLog log : behaviorLogs) {
            if (log.getTargetId() != null) {
                News news = newsService.getById(log.getTargetId());
                if (news != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "read_news");
                    item.put("newsTitle", news.getTitle());
                    item.put("date", log.getCreateTime().toLocalDate());
                    item.put("time", log.getCreateTime());
                    trajectory.add(item);
                }
            }
        }

        // 按时间倒序排序
        trajectory.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("time");
            LocalDateTime timeB = (LocalDateTime) b.get("time");
            return timeB.compareTo(timeA);
        });

        return trajectory;
    }

    @Override
    public Map<String, Object> getLearningSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();

        // 最近30天的学习总时长
        int totalStudyTime = studyRecordService.getTotalDurationByDateRange(userId, LocalDate.now().minusDays(30), LocalDate.now());
        summary.put("totalStudyTime", totalStudyTime);

        // 最近30天的下载资源数量
        List<DownloadHistory> downloadHistories = downloadHistoryService.getDownloadHistoryByUserId(userId);
        long recentDownloads = downloadHistories.stream()
                .filter(history -> {
                    LocalDate downloadDate = history.getDownloadTime().toLocalDate();
                    return !downloadDate.isBefore(LocalDate.now().minusDays(30));
                })
                .count();
        summary.put("recentDownloads", recentDownloads);

        // 最近30天的资讯阅读数量
        long recentNewsReads = userBehaviorLogMapper.selectCount(
                new LambdaQueryWrapper<UserBehaviorLog>()
                        .eq(UserBehaviorLog::getUserId, userId)
                        .eq(UserBehaviorLog::getActionType, "view_news")
                        .ge(UserBehaviorLog::getCreateTime, LocalDate.now().minusDays(30).atStartOfDay())
        );
        summary.put("recentNewsReads", recentNewsReads);

        // 学习轨迹统计
        List<Map<String, Object>> recentTrajectory = getLearningTrajectoryByDateRange(userId, LocalDate.now().minusDays(7), LocalDate.now());
        summary.put("recentTrajectory", recentTrajectory);

        return summary;
    }
}
