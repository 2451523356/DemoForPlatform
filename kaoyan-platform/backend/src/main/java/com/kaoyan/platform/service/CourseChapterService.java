package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.entity.CourseChapter;

import java.util.List;

public interface CourseChapterService extends IService<CourseChapter> {
    
    List<CourseChapter> getChaptersByCourseId(Long courseId);
}
