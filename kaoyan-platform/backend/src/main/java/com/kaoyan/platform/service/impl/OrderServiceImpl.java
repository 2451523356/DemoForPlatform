package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Course;
import com.kaoyan.platform.entity.Order;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.mapper.CourseMapper;
import com.kaoyan.platform.mapper.OrderMapper;
import com.kaoyan.platform.mapper.UserMapper;
import com.kaoyan.platform.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CourseMapper courseMapper;

    @Override
    public PageResult<Order> getOrderList(Integer page, Integer size, Integer status, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(Order::getId);

        Page<Order> orderPage = new Page<>(page, size);
        orderPage = page(orderPage, wrapper);

        List<Order> records = orderPage.getRecords();
        for (Order order : records) {
            if (order.getUserId() != null) {
                User user = userMapper.selectById(order.getUserId());
                if (user != null) {
                    order.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            }
            if (order.getCourseId() != null) {
                Course course = courseMapper.selectById(order.getCourseId());
                if (course != null) {
                    order.setCourseTitle(course.getTitle());
                }
            }
        }

        return new PageResult<>(orderPage.getTotal(), (long) page, (long) size, records);
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
    }
}
