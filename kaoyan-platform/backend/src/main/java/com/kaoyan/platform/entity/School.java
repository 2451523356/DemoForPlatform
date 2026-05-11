package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_school")
public class School extends BaseEntity {

    private String name;
    private String logo;
    private String type;
    private String province;
    private String city;
    private String website;
    private String introduction;
    private Integer ranking;
    private Integer isSelfDrawing;
    private Integer status;
    private String affiliation;
    private Integer masterPoints;
    private Integer doctorPoints;
    private String keyDisciplines;
    private String departments;
    private Integer establishedYear;
}
