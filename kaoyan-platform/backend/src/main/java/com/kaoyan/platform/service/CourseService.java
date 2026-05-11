package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Course;
import com.kaoyan.platform.entity.CourseChapter;
import com.kaoyan.platform.entity.UserCourse;
import com.kaoyan.platform.vo.CourseProgressVO;

import java.util.List;
import java.util.Map;

public interface CourseService extends IService<Course> {
    
    PageResult<Course> getCourseList(Integer page, Integer size, String category, String stage, String form, String subject, String keyword);

    PageResult<Course> getAdminCourseList(Integer page, Integer size, String keyword);
    
    Course getCourseById(Long id);
    
    List<CourseChapter> getCourseChapters(Long courseId);
    
    CourseChapter getChapterById(Long id);
    
    UserCourse getUserCourse(Long userId, Long courseId);
    
    void updateStudyProgress(Long userId, Long courseId, Long chapterId, Integer progress, Integer elapsedSeconds);
    
    void incrementStudentCount(Long courseId);
    
    void buyCourse(Long userId, Long courseId, Integer payType);
    
    List<Map<String, Object>> getUserCourses(Long userId);
    
    void completeChapter(Long userId, Long courseId, Long chapterId);
    
    CourseProgressVO getUserCourseProgress(Long userId, Long courseId);

    void requestRefund(Long userId, Long courseId, String reason);
}
