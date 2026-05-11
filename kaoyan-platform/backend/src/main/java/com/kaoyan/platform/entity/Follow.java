package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_follow")
public class Follow extends BaseEntity {
    
    private Long userId;
    private Long followUserId;
    private Integer status;
}
