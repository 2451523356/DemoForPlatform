package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Post;
import com.kaoyan.platform.mapper.CircleMapper;
import com.kaoyan.platform.mapper.PostMapper;
import com.kaoyan.platform.service.PostService;
import org.springframework.stereotype.Service;

import java.util.*;
import javax.annotation.Resource;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private CircleMapper circleMapper;

    @Override
    public PageResult<Post> getPostList(Integer page, Integer size, Long circleId, String keyword, Integer postType, Long userId, List<Long> followingUserIds) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        wrapper.eq(Post::getDeleted, 0);

        if (circleId != null) {
            wrapper.eq(Post::getCircleId, circleId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword).or().like(Post::getTags, keyword));
        }

        if (postType != null) {
            wrapper.eq(Post::getPostType, postType);
        }

        if (userId != null) {
            wrapper.eq(Post::getUserId, userId);
        }

        if (followingUserIds != null && !followingUserIds.isEmpty()) {
            wrapper.in(Post::getUserId, followingUserIds);
        }

        wrapper.orderByDesc(Post::getIsTop).orderByDesc(Post::getCreateTime);

        Page<Post> postPage = new Page<>(page, size);
        postPage = baseMapper.selectPage(postPage, wrapper);

        // 为每条记录填充作者信息和圈子名称
        List<Post> records = postPage.getRecords();
        for (Post post : records) {
            Post postWithUser = baseMapper.selectPostWithUserById(post.getId());
            if (postWithUser != null) {
                post.setAuthorName(postWithUser.getAuthorName());
                post.setAuthorAvatar(postWithUser.getAuthorAvatar());
            }
            // 填充圈子名称
            if (post.getCircleId() != null) {
                com.kaoyan.platform.entity.Circle circle = circleMapper.selectById(post.getCircleId());
                if (circle != null) {
                    post.setCircleName(circle.getName());
                }
            }
        }

        return new PageResult<>(postPage.getTotal(), (long) page, (long) size, records);
    }

    @Override
    public Post getPostById(Long id) {
        Post post = baseMapper.selectPostWithUserById(id);
        if (post == null) {
            post = getById(id);
        }
        return post;
    }

    @Override
    public void incrementViewCount(Long id) {
        Post post = getById(id);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            updateById(post);
        }
    }

    @Override
    public void incrementLikeCount(Long id) {
        Post post = getById(id);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            updateById(post);
        }
    }

    @Override
    public void incrementCommentCount(Long id) {
        Post post = getById(id);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            updateById(post);
        }
    }

    @Override
    public PageResult<Post> getAdminPostList(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        wrapper.orderByDesc(Post::getId);

        Page<Post> postPage = new Page<>(page, size);
        postPage = baseMapper.selectPage(postPage, wrapper);

        List<Post> records = postPage.getRecords();
        for (Post post : records) {
            Post postWithUser = baseMapper.selectPostWithUserById(post.getId());
            if (postWithUser != null) {
                post.setAuthorName(postWithUser.getAuthorName());
                post.setAuthorAvatar(postWithUser.getAuthorAvatar());
            }
            if (post.getCircleId() != null) {
                com.kaoyan.platform.entity.Circle circle = circleMapper.selectById(post.getCircleId());
                if (circle != null) {
                    post.setCircleName(circle.getName());
                }
            }
        }

        return new PageResult<>(postPage.getTotal(), (long) page, (long) size, records);
    }

    @Override
    public List<Map<String, Object>> getHotTags(int limit) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        wrapper.eq(Post::getDeleted, 0);
        wrapper.select(Post::getTags);
        wrapper.last("LIMIT 500");
        List<Post> posts = baseMapper.selectList(wrapper);

        Map<String, Integer> tagCount = new HashMap<>();
        for (Post post : posts) {
            String tags = post.getTags();
            if (tags == null || tags.isEmpty()) continue;
            for (String tag : tags.split("[,，]")) {
                tag = tag.trim();
                if (!tag.isEmpty()) {
                    tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                }
            }
        }

        return tagCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .map(e -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", e.getKey());
                m.put("count", e.getValue());
                return m;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
