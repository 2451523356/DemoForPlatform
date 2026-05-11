package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_todo")
public class Todo extends BaseEntity {
    
    private Long userId;
    private String title;
    private String description;
    private Integer type;
    private LocalDate targetDate;
    private LocalDate startDate;
    private LocalDate endDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;
    private Integer priority;
    private Integer status;
}
