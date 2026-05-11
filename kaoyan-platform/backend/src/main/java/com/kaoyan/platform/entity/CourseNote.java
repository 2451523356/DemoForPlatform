package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_course_note")
public class CourseNote extends BaseEntity {

    private Long userId;
    private Long courseId;
    private Long chapterId;
    private Integer videoTime;
    private String content;
}
