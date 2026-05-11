package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.StudyRecord;
import com.kaoyan.platform.mapper.StudyRecordMapper;
import com.kaoyan.platform.service.StudyRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudyRecordServiceImpl extends ServiceImpl<StudyRecordMapper, StudyRecord> implements StudyRecordService {
    
    @Override
    public void addStudyRecord(Long userId, Integer duration, String subject, String content, Integer type) {
        StudyRecord record = new StudyRecord();
        record.setUserId(userId);
        record.setStudyDate(LocalDate.now());
        record.setDuration(duration);
        record.setSubject(subject);
        record.setContent(content);
        record.setType(type);
        save(record);
    }
    
    @Override
    public PageResult<StudyRecord> getStudyRecords(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        wrapper.orderByDesc(StudyRecord::getStudyDate);
        
        Page<StudyRecord> recordPage = page(new Page<>(page, size), wrapper);
        
        return new PageResult<>(recordPage.getTotal(), (long) page, (long) size, recordPage.getRecords());
    }
    
    @Override
    public List<StudyRecord> getStudyRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        wrapper.between(StudyRecord::getStudyDate, startDate, endDate);
        wrapper.orderByAsc(StudyRecord::getStudyDate);
        return list(wrapper);
    }
    
    @Override
    public Integer getTotalDurationByDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        wrapper.eq(StudyRecord::getStudyDate, date);
        
        List<StudyRecord> records = list(wrapper);
        return records.stream().mapToInt(StudyRecord::getDuration).sum();
    }
    
    @Override
    public Integer getTotalDurationByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        wrapper.between(StudyRecord::getStudyDate, startDate, endDate);
        
        List<StudyRecord> records = list(wrapper);
        return records.stream().mapToInt(StudyRecord::getDuration).sum();
    }
    
    @Override
    public List<StudyRecord> getRecentRecords(Long userId, Integer limit) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        wrapper.orderByDesc(StudyRecord::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }
    
    @Override
    public java.util.Map<java.time.LocalDate, Integer> getStudyDurationHeatmap(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        wrapper.between(StudyRecord::getStudyDate, startDate, endDate);
        
        List<StudyRecord> records = list(wrapper);
        java.util.Map<java.time.LocalDate, Integer> heatmapData = new java.util.HashMap<>();
        
        // 按日期分组，计算每天的总学习时长
        for (StudyRecord record : records) {
            java.time.LocalDate date = record.getStudyDate();
            int duration = record.getDuration();
            heatmapData.put(date, heatmapData.getOrDefault(date, 0) + duration);
        }
        
        return heatmapData;
    }
}
