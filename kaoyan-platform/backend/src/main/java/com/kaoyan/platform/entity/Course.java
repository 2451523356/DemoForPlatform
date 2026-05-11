package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_course")
public class Course extends BaseEntity {

    private String title;
    private String description;
    private String cover;
    private String outline;
    private String highlights;
    private String targetAudience;
    private String prerequisites;
    private Long teacherId;
    private String categoryId;
    private String subject;
    private String stage;
    private String form;
    private BigDecimal price;
    private Integer lessonCount;
    private Integer duration;
    private Integer studentCount;
    private Double rating;
    private Integer status;
    private String liveUrl;
    private String livePlatform;
    private Integer liveStatus;
    private java.time.LocalDateTime liveStartTime;
    private java.time.LocalDateTime liveEndTime;
}
