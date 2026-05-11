package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_plan_template")
public class PlanTemplate extends BaseEntity {

    private String title;
    private String description;
    private String stage;
    private Integer totalDays;
    private String subjects;
    private String templateData;
    private Integer useCount;
    private Integer status;
}
