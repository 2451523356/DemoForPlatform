package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_live_stream")
public class LiveStream extends BaseEntity {

    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String streamUrl;
    private String playUrl;
    private Integer status; // 0-未开始 1-直播中 2-已结束
    private Integer viewerCount;
    private Integer danmakuCount;
}
