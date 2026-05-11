package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_national_score_line")
public class NationalScoreLine extends BaseEntity {

    private Integer year;
    private String subjectCategory;
    private String degreeType;
    private Integer totalScore;
    private Integer politicalScore;
    private Integer englishScore;
    private Integer course1Score;
    private Integer course2Score;
    private String regionType;
}
