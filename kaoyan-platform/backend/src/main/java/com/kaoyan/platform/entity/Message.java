package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class Message extends BaseEntity {
    
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private Integer isRead;
    private Integer status;
}
