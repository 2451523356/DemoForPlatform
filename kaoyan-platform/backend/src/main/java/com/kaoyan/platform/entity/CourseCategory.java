package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_course_category")
public class CourseCategory extends BaseEntity {
    
    private String name;
    private Long parentId;
    private Integer sort;
    private Integer status;
}
