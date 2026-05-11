package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Comment;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.mapper.CommentMapper;
import com.kaoyan.platform.mapper.UserMapper;
import com.kaoyan.platform.service.CommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserMapper userMapper;

    @Override
    public PageResult<Comment> getCommentList(Integer page, Integer size, Integer status, String keyword) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Comment::getContent, keyword);
        }
        wrapper.orderByDesc(Comment::getId);

        Page<Comment> commentPage = new Page<>(page, size);
        commentPage = page(commentPage, wrapper);

        List<Comment> records = commentPage.getRecords();
        for (Comment comment : records) {
            if (comment.getUserId() != null) {
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    comment.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    comment.setAvatar(user.getAvatar());
                }
            }
        }

        return new PageResult<>(commentPage.getTotal(), (long) page, (long) size, records);
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        return baseMapper.selectCommentsByPostId(postId);
    }

    @Override
    public void incrementLikeCount(Long id) {
        Comment comment = getById(id);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            updateById(comment);
        }
    }
}
