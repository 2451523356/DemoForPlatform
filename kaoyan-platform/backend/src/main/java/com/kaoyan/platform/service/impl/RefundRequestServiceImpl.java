package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Course;
import com.kaoyan.platform.entity.Order;
import com.kaoyan.platform.entity.RefundRequest;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.entity.UserCourse;
import com.kaoyan.platform.mapper.CourseMapper;
import com.kaoyan.platform.mapper.OrderMapper;
import com.kaoyan.platform.mapper.RefundRequestMapper;
import com.kaoyan.platform.mapper.UserCourseMapper;
import com.kaoyan.platform.mapper.UserMapper;
import com.kaoyan.platform.service.MessageService;
import com.kaoyan.platform.service.NotificationService;
import com.kaoyan.platform.service.RefundRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RefundRequestServiceImpl extends ServiceImpl<RefundRequestMapper, RefundRequest> implements RefundRequestService {

    private final OrderMapper orderMapper;
    private final UserCourseMapper userCourseMapper;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public RefundRequestServiceImpl(OrderMapper orderMapper, UserCourseMapper userCourseMapper,
                                    MessageService messageService, NotificationService notificationService,
                                    CourseMapper courseMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.userCourseMapper = userCourseMapper;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<RefundRequest> getRefundList(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<RefundRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RefundRequest::getStatus, status);
        }
        wrapper.orderByDesc(RefundRequest::getId);
        Page<RefundRequest> refundPage = page(new Page<>(page, size), wrapper);

        List<RefundRequest> records = refundPage.getRecords();
        for (RefundRequest req : records) {
            if (req.getUserId() != null) {
                User user = userMapper.selectById(req.getUserId());
                if (user != null) {
                    req.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            }
            if (req.getCourseId() != null) {
                Course course = courseMapper.selectById(req.getCourseId());
                if (course != null) {
                    req.setCourseTitle(course.getTitle());
                }
            }
        }

        return new PageResult<>(refundPage.getTotal(), (long) page, (long) size, records);
    }

    @Override
    public void createRefundRequest(Long userId, Long courseId, Long orderId, String reason) {
        RefundRequest req = new RefundRequest();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setOrderId(orderId);
        req.setReason(reason);
        req.setStatus(0); // 待审核
        save(req);
    }

    @Override
    public void approveRefund(Long id, String rejectReason) {
        RefundRequest req = getById(id);
        if (req == null || req.getStatus() != 0) return;

        req.setStatus(1); // 已通过
        updateById(req);

        // 更新订单状态为已退款（核心操作）��
        if (req.getOrderId() != null) {
            Order order = orderMapper.selectById(req.getOrderId());
            if (order != null && order.getStatus() == 1) {
                order.setStatus(2);
                orderMapper.updateById(order);
            }
        }

        // 删除用户课程关联
        LambdaQueryWrapper<UserCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCourse::getUserId, req.getUserId());
        wrapper.eq(UserCourse::getCourseId, req.getCourseId());
        UserCourse userCourse = userCourseMapper.selectOne(wrapper);
        if (userCourse != null) {
            userCourse.setStatus(0);
            userCourseMapper.updateById(userCourse);
        }

        // 减少学员数
        Course course = courseMapper.selectById(req.getCourseId());
        if (course != null && course.getStudentCount() != null && course.getStudentCount() > 0) {
            course.setStudentCount(course.getStudentCount() - 1);
            courseMapper.updateById(course);
        }

        // 发送通知
        try {
            String courseTitle2 = course != null ? course.getTitle() : "未知课程";
            String msgContent = "您的退款申请（课程：" + courseTitle2 + "）已通过审核，退款将退回您的支付账户。";
            messageService.sendMessage(null, req.getUserId(), msgContent);
            notificationService.createNotification(req.getUserId(), "退款申请已通过", msgContent, "system", req.getId());
        } catch (Exception e) {
            log.warn("发送退款通过通知失败: " + e.getMessage());
        }
    }

    @Override
    public void rejectRefund(Long id, String rejectReason) {
        RefundRequest req = getById(id);
        if (req == null || req.getStatus() != 0) return;

        req.setStatus(2); // 已驳回
        req.setRejectReason(rejectReason);
        updateById(req);

        // 发送通知
        try {
            Course course = courseMapper.selectById(req.getCourseId());
            String courseTitle = course != null ? course.getTitle() : "未知课程";
            String msg = "您的退款申请（课程：" + courseTitle + "）已被驳回。";
            if (rejectReason != null && !rejectReason.trim().isEmpty()) {
                msg += " 理由：" + rejectReason;
            }
            messageService.sendMessage(null, req.getUserId(), msg);
            notificationService.createNotification(req.getUserId(), "退款申请已驳回", msg, "system", req.getId());
        } catch (Exception e) {
            log.warn("发送退款驳回通知失败: " + e.getMessage());
        }
    }
}
