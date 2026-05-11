package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.AdjustmentInfo;

public interface AdjustmentInfoService extends IService<AdjustmentInfo> {

    PageResult<AdjustmentInfo> getList(Integer page, Integer size, Integer year, String schoolName, String major);
}
