package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_course")
public class UserCourse extends BaseEntity {
    
    private Long userId;
    private Long courseId;
    private Long currentChapterId;
    private Integer progress;
    private Integer studyDuration;
    private LocalDateTime lastStudyTime;
    private Integer status;
}
