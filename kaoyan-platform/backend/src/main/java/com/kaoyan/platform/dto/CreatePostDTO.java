package com.kaoyan.platform.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePostDTO {
    
    @NotBlank(message = "帖子标题不能为空")
    private String title;
    
    @NotBlank(message = "话题不能为空")
    private String topic;
    
    @NotBlank(message = "帖子内容不能为空")
    private String content;

    private Integer postType;

    private Long circleId;
}

