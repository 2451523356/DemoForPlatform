package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.Subscription;
import com.kaoyan.platform.mapper.SubscriptionMapper;
import com.kaoyan.platform.service.SubscriptionService;
import com.kaoyan.platform.service.UserBehaviorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {

    @Autowired
    private UserBehaviorLogService userBehaviorLogService;
    
    @Override
    public void subscribe(Long userId, String category) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId);
        wrapper.eq(Subscription::getCategory, category);
        Subscription subscription = getOne(wrapper);
        
        if (subscription == null) {
            subscription = new Subscription();
            subscription.setUserId(userId);
            subscription.setCategory(category);
            subscription.setStatus(1);
            save(subscription);
        } else {
            subscription.setStatus(1);
            updateById(subscription);
        }
        // 记录用户订阅行为
        recordSubscription(userId, category);
    }
    
    @Override
    public void unsubscribe(Long userId, String category) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId);
        wrapper.eq(Subscription::getCategory, category);
        Subscription subscription = getOne(wrapper);
        
        if (subscription != null) {
            subscription.setStatus(0);
            updateById(subscription);
        }
    }
    
    @Override
    public List<String> getUserSubscriptions(Long userId) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId);
        wrapper.eq(Subscription::getStatus, 1);
        wrapper.select(Subscription::getCategory);
        return list(wrapper).stream()
                .map(Subscription::getCategory)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isSubscribed(Long userId, String category) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId);
        wrapper.eq(Subscription::getCategory, category);
        wrapper.eq(Subscription::getStatus, 1);
        return count(wrapper) > 0;
    }

    @Override
    public void recordSubscription(Long userId, String category) {
        // 这里的 targetId 可以是 category 的 ID，如果 category 有对应的 ID 的话。
        // 目前 category 是 String，所以 targetId 暂时设为 null 或 0，或者考虑为 category 建立一个 ID 映射表
        userBehaviorLogService.recordBehavior(userId, "subscribe_category", "category", null);
    }
}
