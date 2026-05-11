package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.entity.Follow;
import com.kaoyan.platform.entity.User;

import java.util.List;

public interface FollowService extends IService<Follow> {
    
    boolean isFollowing(Long userId, Long followUserId);
    
    void follow(Long userId, Long followUserId);
    
    void unfollow(Long userId, Long followUserId);
    
    List<User> getFollowingList(Long userId);
    
    List<User> getFollowerList(Long userId);
    
    long countFollowing(Long userId);
    
    long countFollowers(Long userId);
}
