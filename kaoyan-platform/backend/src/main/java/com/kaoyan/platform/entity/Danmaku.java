package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_danmaku")
public class Danmaku extends BaseEntity {

    private Long liveStreamId;
    private Long userId;
    private String content;
    private Integer type; // 0-普通 1-高级 2-礼物
    private String color;
    private Integer position;
    private LocalDateTime sendTime;
}
