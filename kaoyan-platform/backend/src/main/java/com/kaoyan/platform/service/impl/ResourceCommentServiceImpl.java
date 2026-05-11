package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Resource;
import com.kaoyan.platform.entity.ResourceComment;
import com.kaoyan.platform.mapper.ResourceCommentMapper;
import com.kaoyan.platform.service.ResourceCommentService;
import com.kaoyan.platform.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceCommentServiceImpl extends ServiceImpl<ResourceCommentMapper, ResourceComment> implements ResourceCommentService {
    
    private final ResourceService resourceService;
    
    @Override
    public PageResult<ResourceComment> getResourceComments(Long resourceId, Integer page, Integer size) {
        // 首先获取主评论（parentId = 0）
        LambdaQueryWrapper<ResourceComment> mainWrapper = new LambdaQueryWrapper<>();
        mainWrapper.eq(ResourceComment::getResourceId, resourceId);
        mainWrapper.eq(ResourceComment::getStatus, 1);
        mainWrapper.eq(ResourceComment::getParentId, 0);
        mainWrapper.orderByDesc(ResourceComment::getCreateTime);
        
        Page<ResourceComment> commentPage = page(new Page<>(page, size), mainWrapper);
        java.util.List<ResourceComment> mainComments = commentPage.getRecords();
        
        // 为每个主评论获取回复
        for (ResourceComment mainComment : mainComments) {
            LambdaQueryWrapper<ResourceComment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(ResourceComment::getResourceId, resourceId);
            replyWrapper.eq(ResourceComment::getStatus, 1);
            replyWrapper.eq(ResourceComment::getParentId, mainComment.getId());
            replyWrapper.orderByAsc(ResourceComment::getCreateTime);
            
            java.util.List<ResourceComment> replies = list(replyWrapper);
            mainComment.setReplies(replies);
        }
        
        return new PageResult<>(commentPage.getTotal(), (long) page, (long) size, mainComments);
    }
    
    @Override
    @Transactional
    public void addComment(Long userId, Long resourceId, String content, Integer rating) {
        ResourceComment comment = new ResourceComment();
        comment.setResourceId(resourceId);
        comment.setUserId(userId);
        comment.setParentId(0L); // 默认为主评论
        comment.setContent(content);
        comment.setRating(rating);
        comment.setLikeCount(0);
        comment.setStatus(1);
        save(comment);
        
        if (rating != null && rating >= 1 && rating <= 5) {
            updateResourceRating(resourceId);
        }
    }
    
    @Override
    public boolean deleteComment(Long userId, Long commentId) {
        ResourceComment comment = getById(commentId);
        if (comment == null || !comment.getUserId().equals(userId)) {
            return false;
        }
        comment.setStatus(0);
        updateById(comment);
        if (comment.getRating() != null && comment.getRating() >= 1 && comment.getRating() <= 5) {
            updateResourceRating(comment.getResourceId());
        }
        return true;
    }
    
    @Override
    public void likeComment(Long commentId) {
        ResourceComment comment = getById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            updateById(comment);
        }
    }

    @Override
    @Transactional
    public void replyComment(Long userId, Long resourceId, Long parentId, String content) {
        ResourceComment comment = new ResourceComment();
        comment.setResourceId(resourceId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setRating(null); // 回复评论不需要评分
        comment.setLikeCount(0);
        comment.setStatus(1);
        save(comment);
    }
    
    private void updateResourceRating(Long resourceId) {
        LambdaQueryWrapper<ResourceComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceComment::getResourceId, resourceId);
        wrapper.eq(ResourceComment::getStatus, 1);
        wrapper.isNotNull(ResourceComment::getRating);
        wrapper.ge(ResourceComment::getRating, 1);
        wrapper.le(ResourceComment::getRating, 5);
        
        java.util.List<ResourceComment> comments = list(wrapper);
        if (comments.isEmpty()) {
            return;
        }
        
        double totalRating = comments.stream()
                .mapToInt(comment -> comment.getRating() != null ? comment.getRating() : 0)
                .average()
                .orElse(5.0);
        
        Resource resource = resourceService.getById(resourceId);
        if (resource != null) {
            resource.setRating(java.math.BigDecimal.valueOf(totalRating).setScale(1, java.math.RoundingMode.HALF_UP).doubleValue());
            resource.setRatingCount(comments.size());
            resourceService.updateById(resource);
        }
    }
}
