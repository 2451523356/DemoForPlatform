package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.dto.ChangePasswordDTO;
import com.kaoyan.platform.dto.UpdateProfileDTO;
import com.kaoyan.platform.entity.DownloadHistory;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.service.DownloadHistoryService;
import com.kaoyan.platform.service.PointsRecordService;
import com.kaoyan.platform.service.UserService;
import com.kaoyan.platform.service.FollowService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DownloadHistoryService downloadHistoryService;
    private final PointsRecordService pointsRecordService;
    private final FollowService followService;
    
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setBio(dto.getBio());
        userService.updateById(user);
        return Result.success();
    }
    
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error(400, "当前密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(user);
        return Result.success();
    }
    
    @GetMapping("/profile/{id}")
    public Result<User> getProfileById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        // 隐藏敏感信息
        user.setPassword(null);
        user.setEmail(null);
        user.setPhone(null);
        return Result.success(user);
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        // 隐藏敏感信息
        user.setPassword(null);
        user.setEmail(null);
        user.setPhone(null);
        return Result.success(user);
    }
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("points", user.getPoints() == null ? 0 : user.getPoints());
        stats.put("courses", 0);
        stats.put("followers", followService.countFollowers(userId));
        stats.put("following", followService.countFollowing(userId));
        return Result.success(stats);
    }
    
    @GetMapping("/courses")
    public Result<Object> getMyCourses() {
        return Result.success(Collections.emptyList());
    }
    
    @GetMapping("/downloads")
    public Result<Object> getDownloads() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(downloadHistoryService.getDownloadHistoryByUserId(userId));
    }

    @PostMapping("/signIn")
    public Result<Boolean> signIn() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        boolean signedIn = userService.signIn(userId);
        if (signedIn) {
            Result<Boolean> result = Result.success(true);
            result.setMessage("签到成功，获得5积分");
            return result;
        } else {
            Result<Boolean> result = Result.success(false);
            result.setMessage("今日已签到");
            return result;
        }
    }

    @PostMapping("/reward/share")
    public Result<Void> rewardForShare() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        userService.rewardPointsForShare(userId);
        Result<Void> result = Result.success();
        result.setMessage("分享成功，获得3积分");
        return result;
    }

    @PostMapping("/reward/upload")
    public Result<Void> rewardForUpload() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        userService.rewardPointsForResourceUpload(userId);
        Result<Void> result = Result.success();
        result.setMessage("上传资源成功，获得10积分");
        return result;
    }

    @PostMapping("/reward/comment")
    public Result<Void> rewardForComment() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        userService.rewardPointsForComment(userId);
        Result<Void> result = Result.success();
        result.setMessage("评论成功，获得1积分");
        return result;
    }

    @PostMapping("/reward/like")
    public Result<Void> rewardForLike() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        userService.rewardPointsForLike(userId);
        Result<Void> result = Result.success();
        result.setMessage("点赞成功，获得1积分");
        return result;
    }

    @GetMapping("/points-record")
    public Result<?> getPointsRecord() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(pointsRecordService.getUserPointsRecords(userId, 1, 100));
    }
}

