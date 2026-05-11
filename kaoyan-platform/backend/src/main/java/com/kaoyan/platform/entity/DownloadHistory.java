package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_download_history")
public class DownloadHistory extends BaseEntity {
    private Long userId;
    private Long resourceId;
    private String resourceTitle;
    private String resourceCategory;
    private LocalDateTime downloadTime;
    private Integer pointsConsumed;
    private Long pointsRecordId;
}
