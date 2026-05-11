package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.CourseComment;

import java.util.List;
import java.util.Map;

public interface CourseCommentService extends IService<CourseComment> {

    PageResult<Map<String, Object>> getCommentsByCourseId(Long courseId, Integer page, Integer size);

    void addComment(Long courseId, Long userId, String content, Integer rating);

    void addReply(Long courseId, Long userId, Long parentId, String content);

    void likeComment(Long commentId);

    void deleteComment(Long commentId, Long userId);
}