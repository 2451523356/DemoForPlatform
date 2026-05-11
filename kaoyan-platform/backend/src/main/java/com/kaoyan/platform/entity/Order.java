package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_order")
public class Order extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long courseId;
    private BigDecimal amount;
    private Integer payType;
    private LocalDateTime payTime;
    private Integer status;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String courseTitle;
}
