package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Message;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.mapper.MessageMapper;
import com.kaoyan.platform.service.MessageService;
import com.kaoyan.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    
    private final UserService userService;
    
    @Override
    public void sendMessage(Long fromUserId, Long toUserId, String content) {
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setIsRead(0);
        message.setStatus(1);
        save(message);
    }
    
    @Override
    public PageResult<Message> getMessageList(Long userId, Long otherUserId, Integer page, Integer size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, otherUserId)
                .or().eq(Message::getFromUserId, otherUserId).eq(Message::getToUserId, userId));
        wrapper.orderByAsc(Message::getCreateTime);
        
        Page<Message> messagePage = page(new Page<>(page, size), wrapper);
        
        // 标记对方发送的消息为已读
        List<Message> messages = messagePage.getRecords();
        for (Message message : messages) {
            if (message.getFromUserId() != null && message.getFromUserId().equals(otherUserId) && message.getIsRead() == 0) {
                message.setIsRead(1);
                updateById(message);
            }
        }
        
        return new PageResult<>(messagePage.getTotal(), (long) page, (long) size, messages);
    }
    
    @Override
    public void markAsRead(Long messageId) {
        Message message = getById(messageId);
        if (message != null) {
            message.setIsRead(1);
            updateById(message);
        }
    }
    
    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, userId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.eq(Message::getStatus, 1);
        return (int) count(wrapper);
    }
    
    @Override
    public List<Long> getRecentContacts(Long userId, Integer limit) {
        // 获取最近联系的用户ID
        List<Message> messages = list(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).or().eq(Message::getToUserId, userId))
                .eq(Message::getStatus, 1)
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT " + limit * 2)); // 多查一些以确保能找到不同的联系人
        
        return messages.stream()
                .map(message -> {
                    Long fromId = message.getFromUserId();
                    if (fromId == null) {
                        return message.getToUserId();
                    }
                    return fromId.equals(userId) ? message.getToUserId() : fromId;
                })
                .filter(id -> id != null && !id.equals(userId))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getRecentContactsWithInfo(Long userId, Integer limit) {
        // 获取最近联系的用户ID
        List<Long> contactIds = getRecentContacts(userId, limit);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long contactId : contactIds) {
            User user = userService.getById(contactId);
            if (user != null) {
                Map<String, Object> contact = new HashMap<>();
                contact.put("userId", user.getId());
                contact.put("username", user.getUsername());
                contact.put("nickname", user.getNickname());
                contact.put("avatar", user.getAvatar());
                
                // 获取最后一条消息
                Message lastMessage = getLastMessage(userId, contactId);
                if (lastMessage != null) {
                    contact.put("lastMessage", lastMessage.getContent());
                    contact.put("lastTime", lastMessage.getCreateTime());
                } else {
                    contact.put("lastMessage", "暂无消息");
                    contact.put("lastTime", null);
                }
                
                // 获取未读消息数
                long unreadCount = count(new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getFromUserId, contactId)
                        .eq(Message::getIsRead, 0)
                        .eq(Message::getStatus, 1));
                contact.put("unreadCount", unreadCount);
                
                result.add(contact);
            }
        }
        
        return result;
    }
    
    private Message getLastMessage(Long userId, Long otherUserId) {
        return getOne(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, otherUserId)
                        .or().eq(Message::getFromUserId, otherUserId).eq(Message::getToUserId, userId))
                .eq(Message::getStatus, 1)
                .orderByDesc(Message::getCreateTime));
    }
    
    @Override
    public void markAllAsRead(Long userId, Long fromUserId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, userId);
        wrapper.eq(Message::getFromUserId, fromUserId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.eq(Message::getStatus, 1);
        
        List<Message> messages = list(wrapper);
        for (Message message : messages) {
            message.setIsRead(1);
        }
        updateBatchById(messages);
    }
}