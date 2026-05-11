package com.kaoyan.platform.service;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.DownloadHistory;
import com.kaoyan.platform.entity.News;
import com.kaoyan.platform.entity.StudyRecord;
import com.kaoyan.platform.entity.UserBehaviorLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LearningTrajectoryService {

    PageResult<Map<String, Object>> getLearningTrajectory(Long userId, Integer page, Integer size);

    List<Map<String, Object>> getLearningTrajectoryByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    Map<String, Object> getLearningSummary(Long userId);
}
