package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.CourseChapter;
import com.kaoyan.platform.mapper.CourseChapterMapper;
import com.kaoyan.platform.service.CourseChapterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements CourseChapterService {
    
    @Override
    public List<CourseChapter> getChaptersByCourseId(Long courseId) {
        return list(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .orderByAsc(CourseChapter::getSort)
                .orderByDesc(CourseChapter::getId));
    }
}
