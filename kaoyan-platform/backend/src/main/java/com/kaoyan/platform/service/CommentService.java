package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    
    PageResult<Comment> getCommentList(Integer page, Integer size, Integer status, String keyword);
    
    List<Comment> getCommentsByPostId(Long postId);
    
    void incrementLikeCount(Long id);
}
