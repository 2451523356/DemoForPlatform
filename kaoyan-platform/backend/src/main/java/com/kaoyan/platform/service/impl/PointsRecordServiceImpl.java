package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.PointsRecord;
import com.kaoyan.platform.mapper.PointsRecordMapper;
import com.kaoyan.platform.service.PointsRecordService;
import org.springframework.stereotype.Service;

@Service
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements PointsRecordService {
    
    @Override
    public PageResult<PointsRecord> getUserPointsRecords(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId);
        wrapper.orderByDesc(PointsRecord::getCreateTime);
        
        Page<PointsRecord> pointsRecordPage = page(new Page<>(page, size), wrapper);
        
        return new PageResult<>(pointsRecordPage.getTotal(), (long) page, (long) size, pointsRecordPage.getRecords());
    }
    
    @Override
    public void addPointsRecord(Long userId, Integer points, String type, String description, Long relatedId, String relatedType) {
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setType(type);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        record.setRelatedType(relatedType);
        save(record);
    }
    
    @Override
    public Integer getTotalPointsByType(Long userId, String type) {
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId);
        wrapper.eq(PointsRecord::getType, type);
        return list(wrapper).stream()
                .mapToInt(PointsRecord::getPoints)
                .sum();
    }
}
