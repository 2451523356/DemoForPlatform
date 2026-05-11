package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_course_comment")
public class CourseComment extends BaseEntity {
    
    private Long courseId;
    private Long userId;
    private String content;
    private Integer rating;
    private Integer likeCount;
    private Long parentId;
    private Integer status;
}