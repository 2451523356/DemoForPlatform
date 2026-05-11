package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.ResourceComment;
import com.kaoyan.platform.service.ResourceCommentService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource-comment")
@RequiredArgsConstructor
public class ResourceCommentController {
    
    private final ResourceCommentService resourceCommentService;
    
    @GetMapping("/list/{resourceId}")
    public Result<PageResult<ResourceComment>> getResourceComments(
            @PathVariable Long resourceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(resourceCommentService.getResourceComments(resourceId, page, size));
    }
    
    @PostMapping("/add")
    public Result<Void> addComment(@RequestBody AddCommentRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        resourceCommentService.addComment(userId, request.getResourceId(), request.getContent(), request.getRating());
        return Result.success();
    }
    
    @DeleteMapping("/delete/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        boolean deleted = resourceCommentService.deleteComment(userId, commentId);
        if (!deleted) {
            return Result.error(403, "只能删除自己的评论");
        }
        return Result.success();
    }

    @DeleteMapping("/delete-reply/{replyId}")
    public Result<Void> deleteReply(@PathVariable Long replyId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        boolean deleted = resourceCommentService.deleteComment(userId, replyId);
        if (!deleted) {
            return Result.error(403, "只能删除自己的回复");
        }
        return Result.success();
    }

    @PostMapping("/like/{commentId}")
    public Result<Void> likeComment(@PathVariable Long commentId) {
        resourceCommentService.likeComment(commentId);
        return Result.success();
    }

    @PostMapping("/like-reply/{replyId}")
    public Result<Void> likeReply(@PathVariable Long replyId) {
        resourceCommentService.likeComment(replyId);
        return Result.success();
    }
    
    @PostMapping("/reply/{parentId}")
    public Result<Void> replyComment(@PathVariable Long parentId, @RequestBody ReplyCommentRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        // 不能回复自己的评论
        ResourceComment parentComment = resourceCommentService.getById(parentId);
        if (parentComment != null && parentComment.getUserId().equals(userId)) {
            return Result.error(400, "不能回复自己的评论");
        }
        resourceCommentService.replyComment(userId, request.getResourceId(), parentId, request.getContent());
        return Result.success();
    }
    
    static class AddCommentRequest {
        private Long resourceId;
        private String content;
        private Integer rating;
        
        public Long getResourceId() {
            return resourceId;
        }
        
        public void setResourceId(Long resourceId) {
            this.resourceId = resourceId;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public Integer getRating() {
            return rating;
        }
        
        public void setRating(Integer rating) {
            this.rating = rating;
        }
    }
    
    static class ReplyCommentRequest {
        private Long resourceId;
        private String content;
        
        public Long getResourceId() {
            return resourceId;
        }
        
        public void setResourceId(Long resourceId) {
            this.resourceId = resourceId;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}
