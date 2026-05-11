package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.mapper.UserMapper;
import com.kaoyan.platform.service.PointsRecordService;
import com.kaoyan.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PointsRecordService pointsRecordService;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkEmailExists(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public User getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void addPoints(Long userId, int points) {
        User user = baseMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + points);
            baseMapper.updateById(user);
            pointsRecordService.addPointsRecord(userId, points, "earn", "系统奖励", null, null);
        }
    }

    @Override
    public boolean deductPoints(Long userId, int points, String description, Long relatedId, String relatedType) {
        User user = baseMapper.selectById(userId);
        if (user != null && user.getPoints() >= points) {
            user.setPoints(user.getPoints() - points);
            baseMapper.updateById(user);
            pointsRecordService.addPointsRecord(userId, points, "expense", description, relatedId, relatedType);
            return true;
        }
        return false;
    }
    
    // 保留旧方法以保持兼容性
    public boolean deductPoints(Long userId, int points) {
        return deductPoints(userId, points, "积分消耗", null, null);
    }

    @Override
    public boolean signIn(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (user.getLastSignInTime() != null && user.getLastSignInTime().toLocalDate().isEqual(now.toLocalDate())) {
            // Already signed in today
            return false;
        }

        user.setLastSignInTime(now);
        user.setPoints(user.getPoints() + 5); // Award 5 points for daily sign-in
        baseMapper.updateById(user);
        pointsRecordService.addPointsRecord(userId, 5, "earn", "每日签到奖励", null, null);
        return true;
    }

    @Override
    public void rewardPointsForShare(Long userId) {
        // 直接添加积分并记录分享奖励，避免重复记录
        User user = baseMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + 3);
            baseMapper.updateById(user);
            pointsRecordService.addPointsRecord(userId, 3, "earn", "分享奖励", null, null);
        }
    }

    @Override
    public void rewardPointsForResourceUpload(Long userId) {
        // 直接添加积分并记录资源上传奖励，避免重复记录
        User user = baseMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + 10);
            baseMapper.updateById(user);
            pointsRecordService.addPointsRecord(userId, 10, "earn", "资源上传奖励", null, null);
        }
    }

    @Override
    public void rewardPointsForComment(Long userId) {
        // 直接添加积分并记录评论奖励，避免重复记录
        User user = baseMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + 1);
            baseMapper.updateById(user);
            pointsRecordService.addPointsRecord(userId, 1, "earn", "评论奖励", null, null);
        }
    }

    @Override
    public void rewardPointsForLike(Long userId) {
        // 直接添加积分并记录点赞奖励，避免重复记录
        User user = baseMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + 1);
            baseMapper.updateById(user);
            pointsRecordService.addPointsRecord(userId, 1, "earn", "点赞奖励", null, null);
        }
    }
}
