package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_resource")
public class Resource extends BaseEntity {
    
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String category;
    private String subject;
    private String tags;
    private Integer points;
    private Long uploaderId;
    private Integer downloadCount;
    private Integer viewCount;
    private Double rating;
    private Integer ratingCount;
    private Integer status;
}
