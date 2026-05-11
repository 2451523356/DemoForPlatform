package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_study_record")
public class StudyRecord extends BaseEntity {
    
    private Long userId;
    private LocalDate studyDate;
    private Integer duration;
    private String subject;
    private String content;
    private Integer type;
}
