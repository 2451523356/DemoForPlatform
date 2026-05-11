package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_refund_request")
public class RefundRequest extends BaseEntity {

    private Long userId;
    private Long courseId;
    private Long orderId;
    private String reason;
    private Integer status;
    private String rejectReason;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String courseTitle;
}
