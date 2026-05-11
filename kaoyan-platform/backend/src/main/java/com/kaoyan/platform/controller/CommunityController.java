package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.dto.CreatePostDTO;
import com.kaoyan.platform.entity.Circle;
import com.kaoyan.platform.entity.Comment;
import com.kaoyan.platform.entity.Follow;
import com.kaoyan.platform.entity.Post;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.service.CircleService;
import com.kaoyan.platform.service.CommentService;
import com.kaoyan.platform.service.PostService;
import com.kaoyan.platform.service.FollowService;
import com.kaoyan.platform.utils.SensitiveWordFilter;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    
    private final CircleService circleService;
    private final PostService postService;
    private final CommentService commentService;
    private final FollowService followService;
    private final com.kaoyan.platform.service.UserService userService;
    private final com.kaoyan.platform.service.NotificationService notificationService;
    private final com.kaoyan.platform.service.ReportService reportService;
    
    // 圈子相关
    @GetMapping("/circles")
    public Result<PageResult<Circle>> getCircles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.success(circleService.getCircleList(page, size, type, keyword));
    }
    
    @GetMapping("/circles/university")
    public Result<PageResult<Circle>> getUniversityCircles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) String major) {
        return Result.success(circleService.getUniversityCircles(page, size, university, major));
    }
    
    @GetMapping("/circles/university-major")
    public Result<java.util.List<Circle>> getUniversityMajorCircles(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) String major) {
        return Result.success(circleService.getUniversityMajorCircles(university, major));
    }
    
    @GetMapping("/circle/{id}")
    public Result<Circle> getCircleDetail(@PathVariable Long id) {
        return Result.success(circleService.getCircleById(id));
    }
    
    // 帖子相关
    @GetMapping("/posts")
    public Result<PageResult<Post>> getPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer postType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String followingUserIds) {
        java.util.List<Long> followIds = null;
        if (followingUserIds != null && !followingUserIds.isEmpty()) {
            followIds = java.util.Arrays.stream(followingUserIds.split(","))
                    .map(Long::parseLong).collect(java.util.stream.Collectors.toList());
        }
        return Result.success(postService.getPostList(page, size, circleId, keyword, postType, userId, followIds));
    }
    
    @GetMapping("/post/{id}")
    public Result<Post> getPostDetail(@PathVariable Long id) {
        postService.incrementViewCount(id);
        return Result.success(postService.getPostById(id));
    }

    @PostMapping("/post")
    public Result<Void> createPost(@Valid @RequestBody CreatePostDTO dto) {
        Long userId = UserContext.getUserId();
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setTags(dto.getTopic());
        post.setCircleId(dto.getCircleId());
        post.setPostType(dto.getPostType() != null ? dto.getPostType() : 1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        boolean hasSensitive = SensitiveWordFilter.containsSensitive(post.getContent())
                || SensitiveWordFilter.containsSensitive(post.getTitle());
        post.setStatus(hasSensitive ? 0 : 1);
        postService.save(post);

        // 通知关注者：有新帖子
        try {
            com.kaoyan.platform.entity.User author = userService.getById(userId);
            String authorName = author != null ? (author.getNickname() != null ? author.getNickname() : author.getUsername()) : "用户";
            List<com.kaoyan.platform.entity.User> followers = followService.getFollowerList(userId);
            for (com.kaoyan.platform.entity.User follower : followers) {
                notificationService.createNotification(
                    follower.getId(),
                    "关注动态",
                    "你关注的 " + authorName + " 发布了新帖子：" + post.getTitle(),
                    "follow_post",
                    post.getId()
                );
            }
        } catch (Exception e) {
            // 通知推送失败不影响帖子发布
        }
        return Result.success();
    }
    
    // 评论相关
    @GetMapping("/post/{postId}/comments")
    public Result<List<Comment>> getComments(@PathVariable Long postId) {
        return Result.success(commentService.getCommentsByPostId(postId));
    }
    
    @PostMapping("/comment")
    public Result<Comment> createComment(@RequestBody Comment comment) {
        if (comment.getPostId() == null) {
            return Result.error(400, "帖子 ID 不能为空");
        }
        Long userId = UserContext.getUserId();
        comment.setUserId(userId);
        comment.setLikeCount(0);
        boolean hasSensitive = SensitiveWordFilter.containsSensitive(comment.getContent());
        comment.setStatus(hasSensitive ? 0 : 1);
        commentService.save(comment);
        postService.incrementCommentCount(comment.getPostId());
        return Result.success(comment);
    }
    
    // 点赞相关
    @PostMapping("/post/{id}/like")
    public Result<Boolean> likePost(@PathVariable Long id) {
        postService.incrementLikeCount(id);
        return Result.success(true);
    }
    
    @PostMapping("/comment/{id}/like")
    public Result<Boolean> likeComment(@PathVariable Long id) {
        commentService.incrementLikeCount(id);
        return Result.success(true);
    }

    // 举报接口
    @PostMapping("/report")
    public Result<Void> submitReport(@RequestBody java.util.Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "请先登录");
        Long targetId = body.get("targetId") != null ? Long.valueOf(body.get("targetId").toString()) : null;
        Integer targetType = body.get("targetType") != null ? Integer.valueOf(body.get("targetType").toString()) : 1;
        String reason = body.get("reason") != null ? body.get("reason").toString() : "";
        if (targetId == null || reason.isEmpty()) return Result.error(400, "参数不完整");

        // 检查是否已提交过未审核的举报
        long existCount = reportService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.kaoyan.platform.entity.Report>()
            .eq(com.kaoyan.platform.entity.Report::getUserId, userId)
            .eq(com.kaoyan.platform.entity.Report::getTargetType, targetType)
            .eq(com.kaoyan.platform.entity.Report::getTargetId, targetId)
            .eq(com.kaoyan.platform.entity.Report::getStatus, 0));
        if (existCount > 0) return Result.error(400, "您已举报过该内容，请等待审核处理");

        com.kaoyan.platform.entity.Report report = new com.kaoyan.platform.entity.Report();
        report.setUserId(userId);
        report.setTargetId(targetId);
        report.setTargetType(targetType);
        report.setReason(reason);
        report.setStatus(0);
        reportService.save(report);
        return Result.success();
    }

    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId) && !UserContext.isAdmin()) {
            return Result.error(403, "只能删除自己的评论");
        }
        commentService.removeById(id);
        return Result.success();
    }

    // 删除帖子
    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        Post post = postService.getPostById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        
        // 检查是否是帖子作者或管理员
        if (!post.getUserId().equals(userId) && !UserContext.isAdmin()) {
            return Result.error(403, "无权限删除该帖子");
        }
        
        postService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/hot-tags")
    public Result<List<java.util.Map<String, Object>>> getHotTags() {
        return Result.success(postService.getHotTags(20));
    }

    // 关注相关
    @GetMapping("/follow/check")
    public Result<Boolean> checkFollow(@RequestParam Long followUserId) {
        Long userId = UserContext.getUserId();
        return Result.success(followService.isFollowing(userId, followUserId));
    }
    
    @PostMapping("/follow/{followUserId}")
    public Result<Void> follow(@PathVariable Long followUserId) {
        Long userId = UserContext.getUserId();
        followService.follow(userId, followUserId);
        return Result.success();
    }
    
    @PostMapping("/unfollow/{followUserId}")
    public Result<Void> unfollow(@PathVariable Long followUserId) {
        Long userId = UserContext.getUserId();
        followService.unfollow(userId, followUserId);
        return Result.success();
    }
    
    @GetMapping("/follow/following")
    public Result<List<User>> getFollowingList(@RequestParam(required = false) Long userId) {
        Long queryUserId = userId != null ? userId : UserContext.getUserId();
        return Result.success(followService.getFollowingList(queryUserId));
    }

    @GetMapping("/follow/followers")
    public Result<List<User>> getFollowerList(@RequestParam(required = false) Long userId) {
        Long queryUserId = userId != null ? userId : UserContext.getUserId();
        return Result.success(followService.getFollowerList(queryUserId));
    }
    
    @GetMapping("/follow/count")
    public Result<java.util.Map<String, Object>> getFollowCount(
            @RequestParam(required = false) Long userId) {
        Long currentUserId = userId != null ? userId : UserContext.getUserId();
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("following", followService.countFollowing(currentUserId));
        result.put("followers", followService.countFollowers(currentUserId));
        return Result.success(result);
    }
}
