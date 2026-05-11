package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_resource_comment")
public class ResourceComment extends BaseEntity {
    
    private Long resourceId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer rating;
    private Integer likeCount;
    private Integer status;
    
    // 用于存储回复列表
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<ResourceComment> replies;
    
    public java.util.List<ResourceComment> getReplies() {
        return replies;
    }
    
    public void setReplies(java.util.List<ResourceComment> replies) {
        this.replies = replies;
    }
}
