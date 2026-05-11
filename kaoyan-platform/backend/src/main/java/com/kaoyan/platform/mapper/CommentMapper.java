package com.kaoyan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaoyan.platform.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.nickname AS username, u.avatar AS avatar " +
            "FROM sys_comment c LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 AND c.deleted = 0 " +
            "ORDER BY c.create_time DESC")
    List<Comment> selectCommentsByPostId(@Param("postId") Long postId);
}
