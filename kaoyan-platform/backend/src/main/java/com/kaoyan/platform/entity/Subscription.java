package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_subscription")
public class Subscription extends BaseEntity {
    
    private Long userId;
    private String category;
    private String tags;
    private Integer status;
}
