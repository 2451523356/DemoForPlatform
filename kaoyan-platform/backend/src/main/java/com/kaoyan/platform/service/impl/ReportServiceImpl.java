package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Report;
import com.kaoyan.platform.mapper.ReportMapper;
import com.kaoyan.platform.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    
    @Override
    public PageResult<Report> getReportList(Integer page, Integer size, Integer status, Integer targetType) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        if (targetType != null) {
            wrapper.eq(Report::getTargetType, targetType);
        }
        wrapper.orderByDesc(Report::getId);
        
        Page<Report> reportPage = new Page<>(page, size);
        reportPage = page(reportPage, wrapper);
        
        return new PageResult<>(reportPage.getTotal(), (long) page, (long) size, reportPage.getRecords());
    }
}
