package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.UserBehaviorLog;
import com.kaoyan.platform.mapper.UserBehaviorLogMapper;
import com.kaoyan.platform.service.UserBehaviorLogService;
import com.kaoyan.platform.utils.UserContext;
import org.springframework.stereotype.Service;

@Service
public class UserBehaviorLogServiceImpl extends ServiceImpl<UserBehaviorLogMapper, UserBehaviorLog> implements UserBehaviorLogService {

    @Override
    public void recordBehavior(Long userId, String actionType, String targetType, Long targetId) {
        UserBehaviorLog log = new UserBehaviorLog();
        log.setUserId(userId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        this.save(log);
    }
}
