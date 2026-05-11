package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.Message;
import com.kaoyan.platform.service.MessageService;
import com.kaoyan.platform.service.UserService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/message")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;
    
    // 发送私信
    @PostMapping
    public Result<Void> sendMessage(@RequestBody Message message) {
        Long userId = UserContext.getUserId();
        messageService.sendMessage(userId, message.getToUserId(), message.getContent());
        return Result.success();
    }
    
    // 获取与指定用户的私信列表
    @GetMapping("/chat/{otherUserId}")
    public Result<PageResult<Message>> getMessageList(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getMessageList(userId, otherUserId, page, size));
    }
    
    // 标记单条消息为已读
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }
    
    // 标记与指定用户的所有消息为已读
    @PostMapping("/{otherUserId}/read/all")
    public Result<Void> markAllAsRead(@PathVariable Long otherUserId) {
        Long userId = UserContext.getUserId();
        messageService.markAllAsRead(userId, otherUserId);
        return Result.success();
    }
    
    // 获取未读消息数
    @GetMapping("/unread")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getUnreadCount(userId));
    }
    
    // 获取最近联系人（带用户信息）
    @GetMapping("/contacts")
    public Result<List<Map<String, Object>>> getRecentContacts(
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getRecentContactsWithInfo(userId, limit));
    }

    // 搜索用户发起私聊
    @GetMapping("/search-user")
    public Result<List<Map<String, Object>>> searchUser(@RequestParam(required = false, defaultValue = "") String keyword) {
        Long userId = UserContext.getUserId();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.kaoyan.platform.entity.User> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.kaoyan.platform.entity.User>()
                .eq(com.kaoyan.platform.entity.User::getStatus, 1)
                .ne(com.kaoyan.platform.entity.User::getId, userId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(com.kaoyan.platform.entity.User::getUsername, kw)
                    .or().like(com.kaoyan.platform.entity.User::getNickname, kw));
        }
        wrapper.last("LIMIT 20");
        List<com.kaoyan.platform.entity.User> users = userService.list(wrapper);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.kaoyan.platform.entity.User u : users) {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            m.put("avatar", u.getAvatar());
            result.add(m);
        }
        return Result.success(result);
    }
}