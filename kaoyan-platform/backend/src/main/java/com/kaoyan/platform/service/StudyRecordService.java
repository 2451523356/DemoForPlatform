package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.StudyRecord;

import java.time.LocalDate;
import java.util.List;

public interface StudyRecordService extends IService<StudyRecord> {
    
    void addStudyRecord(Long userId, Integer duration, String subject, String content, Integer type);
    
    PageResult<StudyRecord> getStudyRecords(Long userId, Integer page, Integer size);
    
    List<StudyRecord> getStudyRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    Integer getTotalDurationByDate(Long userId, LocalDate date);
    
    Integer getTotalDurationByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<StudyRecord> getRecentRecords(Long userId, Integer limit);
    
    java.util.Map<java.time.LocalDate, Integer> getStudyDurationHeatmap(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}
