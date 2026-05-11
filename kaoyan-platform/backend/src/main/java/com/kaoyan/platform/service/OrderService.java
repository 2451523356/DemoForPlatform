package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Order;

public interface OrderService extends IService<Order> {
    
    PageResult<Order> getOrderList(Integer page, Integer size, Integer status, String orderNo);
    
    Order getOrderByOrderNo(String orderNo);
}
