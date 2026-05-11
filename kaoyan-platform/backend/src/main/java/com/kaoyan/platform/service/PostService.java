package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Post;

import java.util.List;
import java.util.Map;

public interface PostService extends IService<Post> {
    
    PageResult<Post> getPostList(Integer page, Integer size, Long circleId, String keyword, Integer postType, Long userId, java.util.List<Long> followingUserIds);
    
    Post getPostById(Long id);
    
    void incrementViewCount(Long id);
    
    void incrementLikeCount(Long id);
    
    void incrementCommentCount(Long id);

    List<java.util.Map<String, Object>> getHotTags(int limit);

    PageResult<Post> getAdminPostList(Integer page, Integer size, String keyword);
}
