package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.Course;
import com.kaoyan.platform.entity.CourseChapter;
import com.kaoyan.platform.service.CourseCommentService;
import com.kaoyan.platform.service.CourseService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kaoyan.platform.vo.CourseProgressVO;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    private final CourseCommentService courseCommentService;
    private final com.kaoyan.platform.service.UserService userService;
    
    @GetMapping("/list")
    public Result<PageResult<Course>> getCourseList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String form,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String keyword) {
        return Result.success(courseService.getCourseList(page, size, category, stage, form, subject, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<java.util.Map<String, Object>> getCourseDetail(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return Result.error(404, "课程不存在");
        }
        java.util.Map<String, Object> detail = new java.util.HashMap<>();
        // 复制课程属性
        detail.put("id", course.getId());
        detail.put("title", course.getTitle());
        detail.put("description", course.getDescription());
        detail.put("cover", course.getCover());
        detail.put("outline", course.getOutline());
        detail.put("highlights", course.getHighlights());
        detail.put("targetAudience", course.getTargetAudience());
        detail.put("prerequisites", course.getPrerequisites());
        detail.put("teacherId", course.getTeacherId());
        detail.put("categoryId", course.getCategoryId());
        detail.put("subject", course.getSubject());
        detail.put("stage", course.getStage());
        detail.put("form", course.getForm());
        detail.put("price", course.getPrice());
        detail.put("lessonCount", course.getLessonCount());
        detail.put("duration", course.getDuration());
        detail.put("studentCount", course.getStudentCount());
        detail.put("rating", course.getRating());
        detail.put("createTime", course.getCreateTime());

        // 查询讲师信息
        if (course.getTeacherId() != null) {
            com.kaoyan.platform.entity.User teacher = userService.getById(course.getTeacherId());
            if (teacher != null) {
                java.util.Map<String, Object> teacherInfo = new java.util.HashMap<>();
                teacherInfo.put("id", teacher.getId());
                teacherInfo.put("name", teacher.getNickname() != null ? teacher.getNickname() : teacher.getUsername());
                teacherInfo.put("avatar", teacher.getAvatar());
                teacherInfo.put("title", teacher.getBio());
                teacherInfo.put("intro", teacher.getBio());
                detail.put("teacher", teacherInfo);
            }
        }
        return Result.success(detail);
    }
    
    @GetMapping("/chapters/{courseId}")
    public Result<List<CourseChapter>> getCourseChapters(@PathVariable Long courseId) {
        return Result.success(courseService.getCourseChapters(courseId));
    }
    
    @GetMapping("/chapter/{id}")
    public Result<CourseChapter> getChapterDetail(@PathVariable Long id) {
        return Result.success(courseService.getChapterById(id));
    }

    @GetMapping("/chapter/{id}/download")
    public ResponseEntity<byte[]> downloadChapterFile(@PathVariable Long id) {
        CourseChapter chapter = courseService.getChapterById(id);
        if (chapter == null) {
            return ResponseEntity.notFound().build();
        }
        String content = "【考研学习平台 - 课程配套资料】\n\n"
                + "课程章节：" + chapter.getTitle() + "\n"
                + "资料文件：" + (chapter.getFileName() != null ? chapter.getFileName() : "配套资料") + "\n\n"
                + "--- 示例文件 ---\n"
                + "实际部署时此处为真实的课程配套资料文件。\n";
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String name = chapter.getFileName() != null ? chapter.getFileName() : "资料.txt";
        String encoded;
        try { encoded = java.net.URLEncoder.encode(name, "UTF-8"); }
        catch (java.io.UnsupportedEncodingException e) { encoded = "file.txt"; }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + encoded + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
    
    @PostMapping("/buy")
    public Result<Void> buyCourse(@RequestBody CourseBuyRequest request) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            courseService.buyCourse(userId, request.getCourseId(), request.getPayType());
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "购买失败，请稍后重试");
        }
    }
    
    @GetMapping("/check-purchase/{courseId}")
    public Result<Boolean> checkPurchaseStatus(@PathVariable Long courseId) {
        try {
            Long userId = UserContext.getUserId();
            boolean isPurchased = courseService.getUserCourse(userId, courseId) != null;
            return Result.success(isPurchased);
        } catch (Exception e) {
            // 未登录时返回 false
            return Result.success(false);
        }
    }
    
    @GetMapping("/user-courses")
    public Result<List<Map<String, Object>>> getUserCourses() {
        try {
            Long userId = UserContext.getUserId();
            List<Map<String, Object>> courses = courseService.getUserCourses(userId);
            return Result.success(courses);
        } catch (Exception e) {
            // 未登录时返回空列表
            return Result.success(new ArrayList<>());
        }
    }
    
    @PostMapping("/update-progress")
    public Result<Void> updateProgress(@RequestBody UpdateProgressRequest request) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            courseService.updateStudyProgress(userId, request.getCourseId(), request.getChapterId(), request.getProgress(), request.getElapsedSeconds());
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "更新进度失败");
        }
    }
    
    @PostMapping("/complete-chapter")
    public Result<Void> completeChapter(@RequestBody CompleteChapterRequest request) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            courseService.completeChapter(userId, request.getCourseId(), request.getChapterId());
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "标记章节完成失败");
        }
    }
    
    @GetMapping("/user-progress/{courseId}")
    public Result<CourseProgressVO> getUserCourseProgress(@PathVariable Long courseId) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            CourseProgressVO progress = courseService.getUserCourseProgress(userId, courseId);
            return Result.success(progress);
        } catch (Exception e) {
            return Result.success(new CourseProgressVO());
        }
    }
    
    @GetMapping("/reviews/{courseId}")
    public Result<PageResult<Map<String, Object>>> getCourseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(courseCommentService.getCommentsByCourseId(courseId, page, size));
    }
    
    @PostMapping("/review")
    public Result<Void> addCourseReview(@RequestBody CourseReviewRequest request) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            courseCommentService.addComment(request.getCourseId(), userId, request.getContent(), request.getRating());
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "发表评论失败");
        }
    }
    
    @PostMapping("/review/like/{commentId}")
    public Result<Void> likeReview(@PathVariable Long commentId) {
        courseCommentService.likeComment(commentId);
        return Result.success();
    }

    @DeleteMapping("/review/{commentId}")
    public Result<Void> deleteReview(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "用户未登录");
        courseCommentService.deleteComment(commentId, userId);
        return Result.success();
    }

    @PostMapping("/review/reply")
    public Result<Void> replyReview(@RequestBody ReplyRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "用户未登录");
        courseCommentService.addReply(request.getCourseId(), userId, request.getParentId(), request.getContent());
        return Result.success();
    }

    // 退款申请
    @PostMapping("/refund-request/{courseId}")
    public Result<Void> requestRefund(@PathVariable Long courseId, @RequestBody(required = false) RefundRequestBody body) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "用户未登录");
        try {
            String reason = (body != null && body.getReason() != null) ? body.getReason() : "无理由";
            courseService.requestRefund(userId, courseId, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    static class RefundRequestBody {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    // 课程购买请求参数
    static class CourseBuyRequest {
        private Long courseId;
        private Integer payType;

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public Integer getPayType() {
            return payType;
        }

        public void setPayType(Integer payType) {
            this.payType = payType;
        }
    }
    
    // 更新进度请求参数
    static class UpdateProgressRequest {
        private Long courseId;
        private Long chapterId;
        private Integer progress;
        private Long currentTime;
        private Integer elapsedSeconds;

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getChapterId() { return chapterId; }
        public void setChapterId(Long chapterId) { this.chapterId = chapterId; }
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
        public Long getCurrentTime() { return currentTime; }
        public void setCurrentTime(Long currentTime) { this.currentTime = currentTime; }
        public Integer getElapsedSeconds() { return elapsedSeconds; }
        public void setElapsedSeconds(Integer elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }
    }
    
    // 完成章节请求参数
    static class CompleteChapterRequest {
        private Long courseId;
        private Long chapterId;
        
        public Long getCourseId() {
            return courseId;
        }
        
        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }
        
        public Long getChapterId() {
            return chapterId;
        }
        
        public void setChapterId(Long chapterId) {
            this.chapterId = chapterId;
        }
    }
    
    // 回复请求参数
    static class ReplyRequest {
        private Long courseId;
        private Long parentId;
        private String content;

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // 课程评论请求参数
    static class CourseReviewRequest {
        private Long courseId;
        private String content;
        private Integer rating;
        
        public Long getCourseId() {
            return courseId;
        }
        
        public void setCourseId(Long courseId) {
            this.courseId = courseId;
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
}
