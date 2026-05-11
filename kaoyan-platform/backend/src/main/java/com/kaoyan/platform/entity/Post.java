package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class Post extends BaseEntity {

    private Long userId;
    private String title;
    private String content;
    private String images;
    private Long circleId;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private Integer status;

    /** 帖子类型 1-普通 2-经验 3-答疑 4-资料分享 */
    private Integer postType;

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String authorAvatar;

    @TableField(exist = false)
    private String circleName;
}
