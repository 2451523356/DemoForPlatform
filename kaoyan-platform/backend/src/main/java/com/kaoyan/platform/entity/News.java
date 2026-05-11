package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_news")
public class News extends BaseEntity {
    
    private String title;
    private String content;
    private String summary;
    private String cover;
    private String category;
    private String examStage;
    private String tags;
    private Long authorId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer isTop;
    private Integer status;
}
