package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_circle")
public class Circle extends BaseEntity {
    
    private String name;
    private String description;
    private String icon;
    private String type;
    private String university;
    private String major;
    private Long creatorId;
    private Integer memberCount;
    private Integer postCount;
    private Integer sort;
    private Integer status;
}
