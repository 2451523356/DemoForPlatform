package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_report")
public class Report extends BaseEntity {
    
    private Long userId;
    private Integer targetType;
    private Long targetId;
    private String reason;
    private Integer status;
}
