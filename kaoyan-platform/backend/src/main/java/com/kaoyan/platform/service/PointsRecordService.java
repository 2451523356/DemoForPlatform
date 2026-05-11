package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.PointsRecord;

public interface PointsRecordService extends IService<PointsRecord> {
    
    PageResult<PointsRecord> getUserPointsRecords(Long userId, Integer page, Integer size);
    
    void addPointsRecord(Long userId, Integer points, String type, String description, Long relatedId, String relatedType);
    
    Integer getTotalPointsByType(Long userId, String type);
}
