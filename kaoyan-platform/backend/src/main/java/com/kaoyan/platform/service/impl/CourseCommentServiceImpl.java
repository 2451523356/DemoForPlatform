package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.CourseComment;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.mapper.CourseCommentMapper;
import com.kaoyan.platform.mapper.UserMapper;
import com.kaoyan.platform.service.CourseCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCommentServiceImpl extends ServiceImpl<CourseCommentMapper, CourseComment> implements CourseCommentService {
    
    private final UserMapper userMapper;
    
    @Override
    public PageResult<Map<String, Object>> getCommentsByCourseId(Long courseId, Integer page, Integer size) {
        LambdaQueryWrapper<CourseComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseComment::getCourseId, courseId);
        wrapper.isNull(CourseComment::getParentId);
        wrapper.eq(CourseComment::getStatus, 1);
        wrapper.orderByDesc(CourseComment::getCreateTime);

        Page<CourseComment> commentPage = new Page<>(page, size);
        commentPage = baseMapper.selectPage(commentPage, wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CourseComment comment : commentPage.getRecords()) {
            Map<String, Object> commentMap = buildCommentMap(comment);

            // 查询回复
            LambdaQueryWrapper<CourseComment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(CourseComment::getParentId, comment.getId());
            replyWrapper.eq(CourseComment::getStatus, 1);
            replyWrapper.orderByAsc(CourseComment::getCreateTime);
            List<CourseComment> replies = baseMapper.selectList(replyWrapper);
            List<Map<String, Object>> replyList = new ArrayList<>();
            for (CourseComment reply : replies) {
                replyList.add(buildCommentMap(reply));
            }
            commentMap.put("replies", replyList);

            result.add(commentMap);
        }

        return new PageResult<>(commentPage.getTotal(), (long) page, (long) size, result);
    }

    private Map<String, Object> buildCommentMap(CourseComment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("userId", comment.getUserId());
        map.put("content", comment.getContent());
        map.put("rating", comment.getRating());
        map.put("likeCount", comment.getLikeCount());
        map.put("createTime", comment.getCreateTime());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            map.put("username", user.getNickname() != null ? user.getNickname() : user.getUsername());
            map.put("userAvatar", user.getAvatar());
        }
        return map;
    }
    
    @Override
    public void addComment(Long courseId, Long userId, String content, Integer rating) {
        CourseComment comment = new CourseComment();
        comment.setCourseId(courseId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setRating(rating);
        comment.setLikeCount(0);
        comment.setStatus(1);
        baseMapper.insert(comment);
    }

    @Override
    public void addReply(Long courseId, Long userId, Long parentId, String content) {
        CourseComment reply = new CourseComment();
        reply.setCourseId(courseId);
        reply.setUserId(userId);
        reply.setParentId(parentId);
        reply.setContent(content);
        reply.setLikeCount(0);
        reply.setStatus(1);
        baseMapper.insert(reply);
    }

    @Override
    public void likeComment(Long commentId) {
        CourseComment comment = baseMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount((comment.getLikeCount() == null ? 0 : comment.getLikeCount()) + 1);
            baseMapper.updateById(comment);
        }
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        CourseComment comment = baseMapper.selectById(commentId);
        if (comment != null && comment.getUserId().equals(userId)) {
            comment.setStatus(0);
            baseMapper.updateById(comment);
        }
    }
}