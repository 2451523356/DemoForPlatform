package com.kaoyan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaoyan.platform.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT p.*, u.nickname AS authorName, u.avatar AS authorAvatar " +
            "FROM sys_post p LEFT JOIN sys_user u ON p.user_id = u.id " +
            "WHERE p.deleted = 0 AND p.status = 1 " +
            "ORDER BY p.is_top DESC, p.create_time DESC")
    List<Post> selectPostListWithUser(Page<Post> page);

    @Select("SELECT p.*, u.nickname AS authorName, u.avatar AS authorAvatar " +
            "FROM sys_post p LEFT JOIN sys_user u ON p.user_id = u.id " +
            "WHERE p.id = #{id}")
    Post selectPostWithUserById(@Param("id") Long id);
}
