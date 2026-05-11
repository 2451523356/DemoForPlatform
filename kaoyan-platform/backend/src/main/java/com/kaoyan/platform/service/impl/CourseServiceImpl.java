package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.vo.CourseProgressVO;
import com.kaoyan.platform.entity.Course;
import com.kaoyan.platform.entity.CourseChapter;
import com.kaoyan.platform.entity.UserCourse;
import com.kaoyan.platform.mapper.CourseMapper;
import com.kaoyan.platform.mapper.CourseChapterMapper;
import com.kaoyan.platform.mapper.UserCourseMapper;
import com.kaoyan.platform.service.CourseService;
import com.kaoyan.platform.service.NotificationPushService;
import com.kaoyan.platform.service.StudyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    
    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final UserCourseMapper userCourseMapper;
    private final NotificationPushService notificationPushService;
    private final StudyRecordService studyRecordService;
    private final com.kaoyan.platform.mapper.OrderMapper orderMapper;
    private final com.kaoyan.platform.service.RefundRequestService refundRequestService;

    public CourseServiceImpl(CourseMapper courseMapper, CourseChapterMapper courseChapterMapper, UserCourseMapper userCourseMapper, NotificationPushService notificationPushService, StudyRecordService studyRecordService, com.kaoyan.platform.mapper.OrderMapper orderMapper, com.kaoyan.platform.service.RefundRequestService refundRequestService) {
        this.courseMapper = courseMapper;
        this.courseChapterMapper = courseChapterMapper;
        this.userCourseMapper = userCourseMapper;
        this.notificationPushService = notificationPushService;
        this.studyRecordService = studyRecordService;
        this.orderMapper = orderMapper;
        this.refundRequestService = refundRequestService;
    }
    
    @Override
    public PageResult<Course> getCourseList(Integer page, Integer size, String category, String stage, String form, String subject, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);

        if (category != null && !category.isEmpty()) {
            wrapper.eq(Course::getCategoryId, category);
        }

        if (stage != null && !stage.isEmpty()) {
            wrapper.eq(Course::getStage, stage);
        }

        if (form != null && !form.isEmpty()) {
            wrapper.eq(Course::getForm, form);
        }

        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(Course::getSubject, subject);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Course::getTitle, keyword).or().like(Course::getDescription, keyword));
        }

        wrapper.orderByDesc(Course::getStudentCount).orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = new Page<>(page, size);
        coursePage = courseMapper.selectPage(coursePage, wrapper);

        return new PageResult<>(coursePage.getTotal(), (long) page, (long) size, coursePage.getRecords());
    }
    
    @Override
    public PageResult<Course> getAdminCourseList(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Course::getTitle, keyword).or().like(Course::getDescription, keyword));
        }
        wrapper.orderByDesc(Course::getId);
        Page<Course> coursePage = new Page<>(page, size);
        coursePage = courseMapper.selectPage(coursePage, wrapper);
        return new PageResult<>(coursePage.getTotal(), (long) page, (long) size, coursePage.getRecords());
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id);
    }
    
    @Override
    public boolean save(Course course) {
        boolean saved = super.save(course);
        if (saved && course.getStatus() != null && course.getStatus() == 1) {
            String categoryId = course.getCategoryId() != null ? course.getCategoryId().toString() : null;
            notificationPushService.pushCourseUpdateNotification(
                    course.getId(),
                    course.getTitle(),
                    categoryId
            );
        }
        return saved;
    }

    @Override
    public boolean updateById(Course course) {
        boolean updated = super.updateById(course);
        if (updated && course.getStatus() != null && course.getStatus() == 1) {
            String categoryId = course.getCategoryId() != null ? course.getCategoryId().toString() : null;
            notificationPushService.pushCourseUpdateNotification(
                    course.getId(),
                    course.getTitle(),
                    categoryId
            );
        }
        return updated;
    }

    @Override
    public List<CourseChapter> getCourseChapters(Long courseId) {
        LambdaQueryWrapper<CourseChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseChapter::getCourseId, courseId);
        wrapper.eq(CourseChapter::getStatus, 1);
        wrapper.orderByAsc(CourseChapter::getSort).orderByDesc(CourseChapter::getId);
        return courseChapterMapper.selectList(wrapper);
    }
    
    @Override
    public CourseChapter getChapterById(Long id) {
        return courseChapterMapper.selectById(id);
    }
    
    @Override
    public UserCourse getUserCourse(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return null;
        }
        LambdaQueryWrapper<UserCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCourse::getUserId, userId);
        wrapper.eq(UserCourse::getCourseId, courseId);
        wrapper.eq(UserCourse::getStatus, 1);
        UserCourse userCourse = userCourseMapper.selectOne(wrapper);
        return userCourse;
    }
    
    @Override
    public void updateStudyProgress(Long userId, Long courseId, Long chapterId, Integer progress, Integer elapsedSeconds) {
        UserCourse userCourse = getUserCourse(userId, courseId);
        if (userCourse == null) {
            return; // 未购买课程，不记录进度
        }
        userCourse.setCurrentChapterId(chapterId);
        userCourse.setProgress(progress);
        userCourse.setLastStudyTime(LocalDateTime.now());
        // 累加学习时长（秒转分钟）
        if (elapsedSeconds != null && elapsedSeconds > 0) {
            int currentDuration = userCourse.getStudyDuration() != null ? userCourse.getStudyDuration() : 0;
            userCourse.setStudyDuration(currentDuration + elapsedSeconds);
        }
        userCourseMapper.updateById(userCourse);
    }
    
    @Override
    public void incrementStudentCount(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            Integer currentCount = course.getStudentCount();
            course.setStudentCount(currentCount != null ? currentCount + 1 : 1);
            courseMapper.updateById(course);
        }
    }
    
    @Override
    @Transactional
    public void buyCourse(Long userId, Long courseId, Integer payType) {
        if (userId == null || courseId == null) {
            throw new IllegalArgumentException("用户 ID 和课程 ID 不能为空");
        }

        UserCourse existingUserCourse = getUserCourse(userId, courseId);
        if (existingUserCourse != null) {
            return;
        }

        // 创建用户-课程关联
        UserCourse userCourse = new UserCourse();
        userCourse.setUserId(userId);
        userCourse.setCourseId(courseId);
        userCourse.setCurrentChapterId(null);
        userCourse.setProgress(0);
        userCourse.setStudyDuration(0);
        userCourse.setLastStudyTime(LocalDateTime.now());
        userCourse.setStatus(1);
        userCourseMapper.insert(userCourse);

        // 创建订单记录
        Course course = getById(courseId);
        com.kaoyan.platform.entity.Order order = new com.kaoyan.platform.entity.Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + String.format("%04d", userId.intValue() % 10000));
        order.setUserId(userId);
        order.setCourseId(courseId);
        order.setAmount(course != null ? course.getPrice() : java.math.BigDecimal.ZERO);
        order.setPayType(payType != null ? payType : 0);
        order.setPayTime(LocalDateTime.now());
        order.setStatus(1); // 已支付
        orderMapper.insert(order);

        incrementStudentCount(courseId);
    }
    
    @Override
    public List<Map<String, Object>> getUserCourses(Long userId) {
        LambdaQueryWrapper<UserCourse> userCourseWrapper = new LambdaQueryWrapper<>();
        userCourseWrapper.eq(UserCourse::getUserId, userId);
        userCourseWrapper.eq(UserCourse::getStatus, 1);
        List<UserCourse> userCourses = userCourseMapper.selectList(userCourseWrapper);
        
        if (userCourses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 创建课程ID到用户课程记录的映射
        Map<Long, UserCourse> userCourseMap = userCourses.stream()
                .collect(Collectors.toMap(UserCourse::getCourseId, uc -> uc));
        
        List<Long> courseIds = userCourses.stream()
                .map(UserCourse::getCourseId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.in(Course::getId, courseIds);
        courseWrapper.eq(Course::getStatus, 1);
        List<Course> courses = courseMapper.selectList(courseWrapper);
        
        // 为每个课程添加进度信息
        List<Map<String, Object>> result = new ArrayList<>();
        for (Course course : courses) {
            Map<String, Object> courseMap = new HashMap<>();
            // 使用反射或工具类将Course对象转换为Map
            try {
                Field[] fields = Course.class.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    courseMap.put(field.getName(), field.get(course));
                }
                // 添加上下文相关字段
                courseMap.put("id", course.getId());
                courseMap.put("title", course.getTitle());
                courseMap.put("cover", course.getCover());
                
                // 添加进度信息
                UserCourse userCourse = userCourseMap.get(course.getId());
                if (userCourse != null) {
                    courseMap.put("progress", userCourse.getProgress());
                } else {
                    courseMap.put("progress", 0);
                }
                
                result.add(courseMap);
            } catch (Exception e) {
                log.error("转换课程对象失败", e);
            }
        }
        
        return result;
    }
    
    @Override
    public void completeChapter(Long userId, Long courseId, Long chapterId) {
        CourseChapter chapter = getChapterById(chapterId);
        if (chapter == null) {
            return;
        }
        // 免费章节允许不购买就完成
        if (chapter.getIsFree() != null && chapter.getIsFree() == 1) {
            return; // 免费试看章节仅前端记录，不持久化
        }
        UserCourse userCourse = getUserCourse(userId, courseId);
        if (userCourse == null) {
            throw new IllegalArgumentException("用户未购买该课程");
        }
        List<CourseChapter> chapters = getCourseChapters(courseId);
        if (chapters.isEmpty()) {
            return;
        }
        int currentChapterSort = getChapterSortById(chapters, chapterId);
        int completedCount = 0;
        for (CourseChapter ch : chapters) {
            if (ch.getSort() != null && ch.getSort() <= currentChapterSort) {
                completedCount++;
            }
        }
        int overallProgress = (completedCount * 100) / chapters.size();
        userCourse.setCurrentChapterId(chapterId);
        userCourse.setProgress(Math.min(overallProgress, 100));
        userCourse.setLastStudyTime(LocalDateTime.now());
        userCourseMapper.updateById(userCourse);

        // 记录学习轨迹：自动创建学习记录
        try {
            Course course = getById(courseId);
            String subject = course != null ? course.getSubject() : "综合";
            String content = (course != null ? course.getTitle() : "课程") + " - " + chapter.getTitle();
            int duration = chapter.getDuration() != null ? chapter.getDuration() : 0;
            studyRecordService.addStudyRecord(userId, duration, subject, content, 1); // type=1: 课程学习
        } catch (Exception e) {
            log.warn("创建课程学习记录失败: " + e.getMessage());
        }
    }
    
    @Override
    public CourseProgressVO getUserCourseProgress(Long userId, Long courseId) {
        CourseProgressVO progressVO = new CourseProgressVO();
        
        UserCourse userCourse = getUserCourse(userId, courseId);
        if (userCourse == null) {
            return progressVO;
        }
        
        List<CourseChapter> chapters = getCourseChapters(courseId);
        List<Long> completedChapters = new ArrayList<>();
        Map<Long, Integer> chapterProgress = new HashMap<>();
        
        for (CourseChapter chapter : chapters) {
            if (chapter.getSort() != null && chapter.getSort() < (userCourse.getCurrentChapterId() != null ? 
                getChapterSortById(chapters, userCourse.getCurrentChapterId()) : 0)) {
                completedChapters.add(chapter.getId());
                chapterProgress.put(chapter.getId(), 100);
            } else if (chapter.getId().equals(userCourse.getCurrentChapterId())) {
                chapterProgress.put(chapter.getId(), userCourse.getProgress() != null ? userCourse.getProgress() : 0);
            }
        }
        
        progressVO.setCompletedChapters(completedChapters);
        progressVO.setChapterProgress(chapterProgress);
        
        return progressVO;
    }
    
    @Override
    public void requestRefund(Long userId, Long courseId, String reason) {
        UserCourse userCourse = getUserCourse(userId, courseId);
        if (userCourse == null) {
            throw new IllegalArgumentException("未购买该课程，无法申请退款");
        }
        // 查找已支付订单
        LambdaQueryWrapper<com.kaoyan.platform.entity.Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(com.kaoyan.platform.entity.Order::getUserId, userId);
        orderWrapper.eq(com.kaoyan.platform.entity.Order::getCourseId, courseId);
        orderWrapper.eq(com.kaoyan.platform.entity.Order::getStatus, 1);
        com.kaoyan.platform.entity.Order order = orderMapper.selectOne(orderWrapper);
        if (order == null) {
            throw new IllegalArgumentException("未找到已支付订单");
        }
        // 检查是否已有待审核的退款申请
        LambdaQueryWrapper<com.kaoyan.platform.entity.RefundRequest> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.eq(com.kaoyan.platform.entity.RefundRequest::getUserId, userId);
        refundWrapper.eq(com.kaoyan.platform.entity.RefundRequest::getCourseId, courseId);
        refundWrapper.eq(com.kaoyan.platform.entity.RefundRequest::getStatus, 0);
        long pendingCount = refundRequestService.count(refundWrapper);
        if (pendingCount > 0) {
            throw new IllegalArgumentException("您已有一个待审核的退款申请，请耐心等待");
        }
        refundRequestService.createRefundRequest(userId, courseId, order.getId(), reason);
    }

    private int getChapterSortById(List<CourseChapter> chapters, Long chapterId) {
        return chapters.stream()
                .filter(chapter -> chapter.getId().equals(chapterId))
                .mapToInt(CourseChapter::getSort)
                .findFirst()
                .orElse(0);
    }
}
