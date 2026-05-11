package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_course_chapter")
public class CourseChapter extends BaseEntity {

    private Long courseId;
    private String title;
    private String videoUrl;
    private Integer duration;
    private Integer sort;
    private Integer isFree;
    private String fileUrl;
    private String fileName;
    private Integer status;
}
