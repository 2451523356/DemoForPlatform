package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.ResourceComment;

public interface ResourceCommentService extends IService<ResourceComment> {
    
    PageResult<ResourceComment> getResourceComments(Long resourceId, Integer page, Integer size);
    
    void addComment(Long userId, Long resourceId, String content, Integer rating);
    
    boolean deleteComment(Long userId, Long commentId);
    
    void likeComment(Long commentId);
    
    void replyComment(Long userId, Long resourceId, Long parentId, String content);
}
