package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.entity.Subscription;

import java.util.List;

public interface SubscriptionService extends IService<Subscription> {
    
    void subscribe(Long userId, String category);
    
    void unsubscribe(Long userId, String category);
    
    List<String> getUserSubscriptions(Long userId);
    
    boolean isSubscribed(Long userId, String category);

    void recordSubscription(Long userId, String category);
}
