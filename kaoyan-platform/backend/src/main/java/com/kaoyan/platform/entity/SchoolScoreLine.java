package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_school_score_line")
public class SchoolScoreLine extends BaseEntity {

    private Long schoolId;
    private Integer year;
    private String major;
    private String majorCode;
    private String degreeType;
    private Integer totalScore;
    private Integer politicalScore;
    private Integer englishScore;
    private Integer course1Score;
    private Integer course2Score;
    private Integer admitCount;
    private Integer applicantCount;
    private String retestRate;
    private String sourceUrl;

    @TableField(exist = false)
    private String schoolName;
}
