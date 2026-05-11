package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Circle;
import com.kaoyan.platform.mapper.CircleMapper;
import com.kaoyan.platform.service.CircleService;
import org.springframework.stereotype.Service;

@Service
public class CircleServiceImpl extends ServiceImpl<CircleMapper, Circle> implements CircleService {
    
    @Override
    public PageResult<Circle> getCircleList(Integer page, Integer size, String type, String keyword) {
        LambdaQueryWrapper<Circle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Circle::getStatus, 1);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Circle::getType, type);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Circle::getName, keyword);
        }
        
        wrapper.orderByDesc(Circle::getMemberCount);
        
        Page<Circle> circlePage = page(new Page<>(page, size), wrapper);
        
        return new PageResult<>(circlePage.getTotal(), (long) page, (long) size, circlePage.getRecords());
    }
    
    @Override
    public Circle getCircleById(Long id) {
        return getById(id);
    }
    
    @Override
    public void incrementMemberCount(Long circleId) {
        Circle circle = getById(circleId);
        if (circle != null) {
            circle.setMemberCount(circle.getMemberCount() + 1);
            updateById(circle);
        }
    }
    
    @Override
    public void incrementPostCount(Long circleId) {
        Circle circle = getById(circleId);
        if (circle != null) {
            circle.setPostCount(circle.getPostCount() + 1);
            updateById(circle);
        }
    }
    
    @Override
    public PageResult<Circle> getUniversityCircles(Integer page, Integer size, String university, String major) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Circle> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        if (university != null && !university.isEmpty()) {
            wrapper.eq(Circle::getUniversity, university);
        }
        
        if (major != null && !major.isEmpty()) {
            wrapper.eq(Circle::getMajor, major);
        }
        
        wrapper.eq(Circle::getStatus, 1);
        wrapper.orderByDesc(Circle::getMemberCount);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Circle> circlePage = page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
        
        return new PageResult<>(circlePage.getTotal(), (long) page, (long) size, circlePage.getRecords());
    }
    
    @Override
    public java.util.List<Circle> getUniversityMajorCircles(String university, String major) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Circle> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        if (university != null && !university.isEmpty()) {
            wrapper.eq(Circle::getUniversity, university);
        }
        
        if (major != null && !major.isEmpty()) {
            wrapper.eq(Circle::getMajor, major);
        }
        
        wrapper.eq(Circle::getStatus, 1);
        wrapper.orderByDesc(Circle::getMemberCount);
        
        return list(wrapper);
    }
}
