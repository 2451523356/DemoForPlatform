package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_behavior_log")
public class UserBehaviorLog extends BaseEntity {

    private Long userId;
    private String actionType; // e.g., view_news, like_news, download_resource, view_resource, subscribe_category
    private String targetType; // e.g., news, resource, category
    private Long targetId;
}
