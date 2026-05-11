package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_major_catalog")
public class MajorCatalog extends BaseEntity {
    private String code;
    private String name;
    private String category;
    private String firstLevel;
    private String degreeType;
    private String description;
    private String examSubjects;
    private String employment;
    private Integer sort;
    private Integer status;
}
