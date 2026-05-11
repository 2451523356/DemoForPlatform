package com.kaoyan.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_school_compare")
public class UserSchoolCompare extends BaseEntity {
    private Long userId;
    private String schoolIds;
    private String note;
}
