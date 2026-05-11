package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.Follow;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.mapper.FollowMapper;
import com.kaoyan.platform.service.FollowService;
import com.kaoyan.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {
    
    private final UserService userService;
    
    @Override
    public boolean isFollowing(Long userId, Long followUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getFollowUserId, followUserId);
        wrapper.eq(Follow::getStatus, 1);
        return count(wrapper) > 0;
    }
    
    @Override
    public void follow(Long userId, Long followUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getFollowUserId, followUserId);
        
        Follow follow = getOne(wrapper);
        if (follow == null) {
            follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            follow.setStatus(1);
            save(follow);
        } else {
            follow.setStatus(1);
            updateById(follow);
        }
    }
    
    @Override
    public void unfollow(Long userId, Long followUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getFollowUserId, followUserId);
        
        Follow follow = getOne(wrapper);
        if (follow != null) {
            follow.setStatus(0);
            updateById(follow);
        }
    }
    
    @Override
    public List<User> getFollowingList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getStatus, 1);
        wrapper.orderByDesc(Follow::getCreateTime);
        List<Follow> follows = list(wrapper);
        return follows.stream()
                .map(follow -> userService.getById(follow.getFollowUserId()))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> getFollowerList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowUserId, userId);
        wrapper.eq(Follow::getStatus, 1);
        wrapper.orderByDesc(Follow::getCreateTime);
        List<Follow> follows = list(wrapper);
        return follows.stream()
                .map(follow -> userService.getById(follow.getUserId()))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countFollowing(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getStatus, 1);
        return count(wrapper);
    }
    
    @Override
    public long countFollowers(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowUserId, userId);
        wrapper.eq(Follow::getStatus, 1);
        return count(wrapper);
    }
}
