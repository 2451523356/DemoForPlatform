package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Report;

public interface ReportService extends IService<Report> {
    
    PageResult<Report> getReportList(Integer page, Integer size, Integer status, Integer targetType);
}
