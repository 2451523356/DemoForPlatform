package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_adjustment_info")
public class AdjustmentInfo extends BaseEntity {

    private String schoolName;
    private String major;
    private String requirement;
    private String contact;
    private String sourceUrl;
    private Integer year;
    private LocalDate publishDate;
    private Integer status;
}
