package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.entity.UserBehaviorLog;

public interface UserBehaviorLogService extends IService<UserBehaviorLog> {
    void recordBehavior(Long userId, String actionType, String targetType, Long targetId);
}
