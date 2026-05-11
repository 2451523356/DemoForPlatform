package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.entity.User;

public interface UserService extends IService<User> {

    User getByUsername(String username);

    boolean checkUsernameExists(String username);

    boolean checkEmailExists(String email);

    boolean removeById(Long id);

    User getById(Long id);

    void addPoints(Long userId, int points);

    boolean signIn(Long userId);

    void rewardPointsForShare(Long userId);

    void rewardPointsForResourceUpload(Long userId);

    void rewardPointsForComment(Long userId);

    void rewardPointsForLike(Long userId);
    
    boolean deductPoints(Long userId, int points, String description, Long relatedId, String relatedType);
}
