package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Circle;

public interface CircleService extends IService<Circle> {
    
    PageResult<Circle> getCircleList(Integer page, Integer size, String type, String keyword);
    
    PageResult<Circle> getUniversityCircles(Integer page, Integer size, String university, String major);
    
    Circle getCircleById(Long id);
    
    void incrementMemberCount(Long circleId);
    
    void incrementPostCount(Long circleId);
    
    java.util.List<Circle> getUniversityMajorCircles(String university, String major);
}
