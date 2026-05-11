package com.kaoyan.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.entity.News;
import com.kaoyan.platform.entity.Resource;
import com.kaoyan.platform.entity.Course;
import com.kaoyan.platform.entity.Post;
import com.kaoyan.platform.entity.CourseCategory;
import com.kaoyan.platform.entity.CourseChapter;
import com.kaoyan.platform.entity.Order;
import com.kaoyan.platform.entity.Comment;
import com.kaoyan.platform.entity.Report;
import com.kaoyan.platform.service.UserService;
import com.kaoyan.platform.service.NewsService;
import com.kaoyan.platform.service.ResourceService;
import com.kaoyan.platform.service.CourseService;
import com.kaoyan.platform.service.PostService;
import com.kaoyan.platform.service.CourseCategoryService;
import com.kaoyan.platform.service.CourseChapterService;
import com.kaoyan.platform.service.OrderService;
import com.kaoyan.platform.service.CommentService;
import com.kaoyan.platform.service.ReportService;
import com.kaoyan.platform.service.RefundRequestService;
import com.kaoyan.platform.service.MessageService;
import com.kaoyan.platform.service.NotificationService;
import com.kaoyan.platform.entity.RefundRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final NewsService newsService;
    private final ResourceService resourceService;
    private final CourseService courseService;
    private final PostService postService;
    private final CourseCategoryService courseCategoryService;
    private final CourseChapterService courseChapterService;
    private final OrderService orderService;
    private final CommentService commentService;
    private final ReportService reportService;
    private final RefundRequestService refundRequestService;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final com.kaoyan.platform.mapper.UserCourseMapper userCourseMapper;
    
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getId);

        Page<User> userPage = new Page<>(page, size);
        userPage = userService.page(userPage, wrapper);
        return Result.success(new PageResult<>(userPage.getTotal(), (long) page, (long) size, userPage.getRecords()));
    }
    
    @PutMapping("/user/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "状态值必须为 0 或 1");
        }
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }
    
    @GetMapping("/news")
    public Result<PageResult<News>> getNews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(newsService.getAdminNewsList(page, size, keyword));
    }
    
    @PostMapping("/news")
    public Result<Void> addNews(@RequestBody News news) {
        newsService.save(news);
        return Result.success();
    }
    
    @PutMapping("/news/{id}")
    public Result<Void> updateNews(@PathVariable Long id, @RequestBody News news) {
        news.setId(id);
        newsService.updateById(news);
        return Result.success();
    }
    
    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        newsService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/resources")
    public Result<PageResult<Resource>> getResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Resource::getTitle, keyword).or().like(Resource::getDescription, keyword));
        }
        wrapper.orderByDesc(Resource::getId);
        Page<Resource> resourcePage = new Page<>(page, size);
        resourcePage = resourceService.page(resourcePage, wrapper);
        return Result.success(new PageResult<>(resourcePage.getTotal(), (long) page, (long) size, resourcePage.getRecords()));
    }
    
    @PostMapping("/resource")
    public Result<Void> addResource(@RequestBody Resource resource) {
        resource.setDownloadCount(resource.getDownloadCount() != null ? resource.getDownloadCount() : 0);
        resource.setViewCount(resource.getViewCount() != null ? resource.getViewCount() : 0);
        resource.setRating(resource.getRating() != null ? resource.getRating() : 0.0);
        resource.setRatingCount(resource.getRatingCount() != null ? resource.getRatingCount() : 0);
        resource.setStatus(resource.getStatus() != null ? resource.getStatus() : 0);
        resourceService.save(resource);
        return Result.success();
    }
    
    @PutMapping("/resource/{id}")
    public Result<Void> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        resource.setId(id);
        resourceService.updateById(resource);
        return Result.success();
    }
    
    @DeleteMapping("/resource/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        resourceService.removeById(id);
        return Result.success();
    }
    
    @PutMapping("/resource/{id}/status")
    public Result<Void> updateResourceStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status < 0 || status > 2)) {
            return Result.error(400, "状态值必须为 0-待审核 1-审核通过 2-审核拒绝");
        }
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        resource.setStatus(status);
        resourceService.updateById(resource);

        // 审核通过后奖励上传者20积分并发送通知
        if (status == 1 && resource.getUploaderId() != null) {
            try {
                userService.addPoints(resource.getUploaderId(), 20);
                notificationService.createNotification(resource.getUploaderId(), "资源审核通过",
                    "您上传的资源《" + resource.getTitle() + "》已通过审核，获得20积分奖励！", "system", resource.getId());
            } catch (Exception e) {
                // 积分奖励失败不影响审核操作
            }
        }

        return Result.success();
    }
    
    @GetMapping("/resources/pending")
    public Result<PageResult<Resource>> getPendingResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 0);
        wrapper.orderByDesc(Resource::getId);
        
        Page<Resource> resourcePage = new Page<>(page, size);
        resourcePage = resourceService.page(resourcePage, wrapper);
        return Result.success(new PageResult<>(resourcePage.getTotal(), (long) page, (long) size, resourcePage.getRecords()));
    }
    
    @GetMapping("/courses")
    public Result<PageResult<Course>> getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(courseService.getAdminCourseList(page, size, keyword));
    }
    
    @PostMapping("/course")
    public Result<Void> addCourse(@RequestBody Course course) {
        courseService.save(course);
        return Result.success();
    }
    
    @PutMapping("/course/{id}")
    public Result<Void> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return Result.success();
    }
    
    @DeleteMapping("/course/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/posts")
    public Result<PageResult<Post>> getPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(postService.getAdminPostList(page, size, keyword));
    }
    
    @PutMapping("/post/{id}/status")
    public Result<Void> updatePostStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status < 0 || status > 2)) {
            return Result.error(400, "状态值必须为 0-待审核 1-通过 2-拒绝");
        }
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        post.setStatus(status);
        postService.updateById(post);
        return Result.success();
    }
    
    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/dashboard")
    public Result<Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("userCount", userService.count());
        dashboard.put("newsCount", newsService.count());
        dashboard.put("courseCount", courseService.count());
        dashboard.put("postCount", postService.count());
        dashboard.put("orderCount", orderService.count());
        dashboard.put("commentCount", commentService.count());

        // 用户增长趋势（最近6个月）
        List<Map<String, Object>> userTrend = new ArrayList<>();
        String[] months = {"1月","2月","3月","4月","5月","6月",
                          "7月","8月","9月","10月","11月","12月"};
        java.time.LocalDate now = java.time.LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            java.time.LocalDate monthEnd = monthStart.plusMonths(1);
            long count = userService.count(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, monthStart)
                    .lt(User::getCreateTime, monthEnd));
            Map<String, Object> item = new HashMap<>();
            item.put("month", months[monthStart.getMonthValue() - 1]);
            item.put("count", count);
            userTrend.add(item);
        }
        dashboard.put("userTrend", userTrend);

        // 资源下载排行（Top 10）
        List<Resource> topResources = resourceService.list(new LambdaQueryWrapper<Resource>()
                .orderByDesc(Resource::getDownloadCount)
                .last("limit 10"));
        List<Map<String, Object>> downloadRank = new ArrayList<>();
        for (Resource r : topResources) {
            Map<String, Object> item = new HashMap<>();
            item.put("title", r.getTitle());
            item.put("count", r.getDownloadCount());
            downloadRank.add(item);
        }
        dashboard.put("downloadRank", downloadRank);

        // 课程销售情况：已支付订单数和总销售额
        long paidOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1));
        dashboard.put("paidOrderCount", paidOrders);

        // 总销售收入（所有已支付订单金额之和）
        List<Order> allPaidOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1));
        double totalRevenue = allPaidOrders.stream().mapToDouble(o -> o.getAmount() != null ? o.getAmount().doubleValue() : 0).sum();
        dashboard.put("totalRevenue", totalRevenue);

        // 课程销售额统计（最近6个月）
        List<Map<String, Object>> salesTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            java.time.LocalDate monthEnd = monthStart.plusMonths(1);
            List<Order> monthOrders = orderService.list(new LambdaQueryWrapper<Order>()
                    .eq(Order::getStatus, 1)
                    .ge(Order::getPayTime, monthStart)
                    .lt(Order::getPayTime, monthEnd));
            double totalAmount = monthOrders.stream().mapToDouble(o -> o.getAmount() != null ? o.getAmount().doubleValue() : 0).sum();
            Map<String, Object> item = new HashMap<>();
            item.put("month", months[monthStart.getMonthValue() - 1]);
            item.put("amount", totalAmount);
            salesTrend.add(item);
        }
        dashboard.put("salesTrend", salesTrend);

        // 社区活跃度（最近6个月新增帖子数）
        List<Map<String, Object>> activityTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            java.time.LocalDate monthEnd = monthStart.plusMonths(1);
            long count = postService.count(new LambdaQueryWrapper<Post>()
                    .ge(Post::getCreateTime, monthStart)
                    .lt(Post::getCreateTime, monthEnd));
            Map<String, Object> item = new HashMap<>();
            item.put("month", months[monthStart.getMonthValue() - 1]);
            item.put("count", count);
            activityTrend.add(item);
        }
        dashboard.put("activityTrend", activityTrend);

        List<User> latestUsers = userService.list(new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime)
                .last("limit 5"));
        dashboard.put("latestUsers", latestUsers);

        List<Post> latestPosts = postService.list(new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreateTime)
                .last("limit 5"));
        for (Post post : latestPosts) {
            Post postWithUser = postService.getPostById(post.getId());
            if (postWithUser != null) {
                post.setAuthorName(postWithUser.getAuthorName());
                post.setAuthorAvatar(postWithUser.getAuthorAvatar());
            }
        }
        dashboard.put("latestPosts", latestPosts);

        return Result.success(dashboard);
    }
    
    // 课程分类管理
    @GetMapping("/course-categories")
    public Result<List<CourseCategory>> getCourseCategories() {
        return Result.success(courseCategoryService.getAdminCategoryTree());
    }
    
    @GetMapping("/course-categories/{parentId}")
    public Result<List<CourseCategory>> getCategoriesByParentId(@PathVariable Long parentId) {
        return Result.success(courseCategoryService.getCategoriesByParentId(parentId));
    }
    
    @PostMapping("/course-category")
    public Result<Void> addCourseCategory(@RequestBody CourseCategory category) {
        courseCategoryService.save(category);
        return Result.success();
    }
    
    @PutMapping("/course-category/{id}")
    public Result<Void> updateCourseCategory(@PathVariable Long id, @RequestBody CourseCategory category) {
        category.setId(id);
        courseCategoryService.updateById(category);
        return Result.success();
    }
    
    @DeleteMapping("/course-category/{id}")
    public Result<Void> deleteCourseCategory(@PathVariable Long id) {
        // 检查是否有子分类
        long childCount = courseCategoryService.count(new LambdaQueryWrapper<CourseCategory>()
                .eq(CourseCategory::getParentId, id));
        if (childCount > 0) {
            return Result.error(400, "该分类下有子分类，无法删除");
        }
        // 检查是否有课程关联该分类
        long courseCount = courseService.count(new LambdaQueryWrapper<Course>()
                .eq(Course::getCategoryId, String.valueOf(id)));
        if (courseCount > 0) {
            return Result.error(400, "该分类下有 " + courseCount + " 门课程，无法删除");
        }
        courseCategoryService.removeById(id);
        return Result.success();
    }
    
    // 课程章节管理
    @GetMapping("/course/{courseId}/chapters")
    public Result<List<CourseChapter>> getCourseChapters(@PathVariable Long courseId) {
        return Result.success(courseChapterService.getChaptersByCourseId(courseId));
    }
    
    @PostMapping("/course-chapter")
    public Result<Void> addCourseChapter(@RequestBody CourseChapter chapter) {
        courseChapterService.save(chapter);
        return Result.success();
    }
    
    @PutMapping("/course-chapter/{id}")
    public Result<Void> updateCourseChapter(@PathVariable Long id, @RequestBody CourseChapter chapter) {
        chapter.setId(id);
        courseChapterService.updateById(chapter);
        return Result.success();
    }
    
    @DeleteMapping("/course-chapter/{id}")
    public Result<Void> deleteCourseChapter(@PathVariable Long id) {
        courseChapterService.removeById(id);
        return Result.success();
    }
    
    // 订单管理
    @GetMapping("/orders")
    public Result<PageResult<Order>> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo) {
        return Result.success(orderService.getOrderList(page, size, status, orderNo));
    }
    
    @PutMapping("/order/{id}/refund")
    public Result<Void> refundOrder(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (order.getStatus() != 1) {
            return Result.error(400, "只能退款已支付的订单");
        }
        order.setStatus(2); // 2-已退款
        orderService.updateById(order);

        // 解除用户课程关联
        com.kaoyan.platform.entity.UserCourse userCourse = courseService.getUserCourse(order.getUserId(), order.getCourseId());
        if (userCourse != null) {
            userCourse.setStatus(0);
            userCourseMapper.updateById(userCourse);
        }

        // 减少学员数
        Course course = courseService.getById(order.getCourseId());
        if (course != null && course.getStudentCount() != null && course.getStudentCount() > 0) {
            course.setStudentCount(course.getStudentCount() - 1);
            courseService.updateById(course);
        }

        // 通知用户
        try {
            String courseTitle2 = course != null ? course.getTitle() : "未知课程";
            String msgContent = "您的订单 " + order.getOrderNo() + "（课程：" + courseTitle2 + "）已由管理员退款。";
            messageService.sendMessage(null, order.getUserId(), msgContent);
            notificationService.createNotification(order.getUserId(), "订单已退款", msgContent, "system", order.getId());
        } catch (Exception ignored) {
            // 通知失败不影响退款操作
        }

        return Result.success();
    }
    
    // 评论管理
    @GetMapping("/comments")
    public Result<PageResult<Comment>> getComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(commentService.getCommentList(page, size, status, keyword));
    }
    
    @PutMapping("/comment/{id}/status")
    public Result<Void> updateCommentStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "状态值必须为 0 或 1");
        }
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        comment.setStatus(status);
        commentService.updateById(comment);
        return Result.success();
    }
    
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.removeById(id);
        return Result.success();
    }
    
    // 举报管理
    @GetMapping("/reports")
    public Result<PageResult<Report>> getReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer targetType) {
        return Result.success(reportService.getReportList(page, size, status, targetType));
    }
    
    @PutMapping("/report/{id}/status")
    public Result<Void> updateReportStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status < 0 || status > 2)) {
            return Result.error(400, "状态值必须为 0-待处理 1-已处理 2-驳回");
        }
        Report report = reportService.getById(id);
        if (report == null) {
            return Result.error(404, "举报记录不存在");
        }
        report.setStatus(status);
        reportService.updateById(report);

        String targetTypeName;
        switch (report.getTargetType() != null ? report.getTargetType() : 0) {
            case 1: targetTypeName = "帖子"; break;
            case 2: targetTypeName = "评论"; break;
            case 3: targetTypeName = "用户"; break;
            default: targetTypeName = "内容"; break;
        }

        // 已处理：对实际内容进行处理（帖子/评论下架）
        if (status == 1) {
            if (report.getTargetType() != null && report.getTargetId() != null) {
                if (report.getTargetType() == 1) {
                    // 下架帖子
                    Post post = postService.getById(report.getTargetId());
                    if (post != null) {
                        post.setStatus(0);
                        postService.updateById(post);
                    }
                } else if (report.getTargetType() == 2) {
                    // 下架评论
                    Comment comment = commentService.getById(report.getTargetId());
                    if (comment != null) {
                        comment.setStatus(0);
                        commentService.updateById(comment);
                    }
                }
            }
        }

        // 通知举报人处理结果
        if (report.getUserId() != null) {
            String statusName = status == 1 ? "已处理（内容已下架）" : "已驳回";
            String msg = "您举报的" + targetTypeName + "（ID:" + report.getTargetId() + "）已被管理员处理，处理结果：" + statusName + "。感谢您的监督！";
            try {
                messageService.sendMessage(null, report.getUserId(), msg);
                notificationService.createNotification(report.getUserId(), "举报处理结果", msg, "system", report.getId());
            } catch (Exception ignored) {
                // 通知发送失败不影响举报处理
            }
        }

        return Result.success();
    }
    
    @DeleteMapping("/report/{id}")
    public Result<Void> deleteReport(@PathVariable Long id) {
        reportService.removeById(id);
        return Result.success();
    }

    // 退款审核管理
    @GetMapping("/refunds")
    public Result<PageResult<RefundRequest>> getRefunds(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(refundRequestService.getRefundList(page, size, status));
    }

    @PutMapping("/refund/{id}/approve")
    public Result<Void> approveRefund(@PathVariable Long id) {
        RefundRequest req = refundRequestService.getById(id);
        if (req == null) {
            return Result.error(404, "退款申请不存在");
        }
        if (req.getStatus() != 0) {
            return Result.error(400, "该申请已处理");
        }
        refundRequestService.approveRefund(id, null);
        return Result.success();
    }

    @PutMapping("/refund/{id}/reject")
    public Result<Void> rejectRefund(@PathVariable Long id, @RequestBody RejectBody body) {
        RefundRequest req = refundRequestService.getById(id);
        if (req == null) {
            return Result.error(404, "退款申请不存在");
        }
        if (req.getStatus() != 0) {
            return Result.error(400, "该申请已处理");
        }
        String reason = body != null ? body.getReason() : null;
        refundRequestService.rejectRefund(id, reason);
        return Result.success();
    }

    static class RejectBody {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
