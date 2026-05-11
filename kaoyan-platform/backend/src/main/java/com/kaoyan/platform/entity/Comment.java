package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_comment")
public class Comment extends BaseEntity {
    
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String username;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String avatar;
}
