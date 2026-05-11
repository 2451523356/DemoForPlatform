package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.AdjustmentInfo;
import com.kaoyan.platform.mapper.AdjustmentInfoMapper;
import com.kaoyan.platform.service.AdjustmentInfoService;
import org.springframework.stereotype.Service;

@Service
public class AdjustmentInfoServiceImpl extends ServiceImpl<AdjustmentInfoMapper, AdjustmentInfo> implements AdjustmentInfoService {

    @Override
    public PageResult<AdjustmentInfo> getList(Integer page, Integer size, Integer year, String schoolName, String major) {
        LambdaQueryWrapper<AdjustmentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdjustmentInfo::getStatus, 1);

        if (year != null) {
            wrapper.eq(AdjustmentInfo::getYear, year);
        }

        if (schoolName != null && !schoolName.isEmpty()) {
            wrapper.like(AdjustmentInfo::getSchoolName, schoolName);
        }

        if (major != null && !major.isEmpty()) {
            wrapper.like(AdjustmentInfo::getMajor, major);
        }

        wrapper.orderByDesc(AdjustmentInfo::getPublishDate).orderByDesc(AdjustmentInfo::getCreateTime);

        Page<AdjustmentInfo> infoPage = page(new Page<>(page, size), wrapper);

        return new PageResult<>(infoPage.getTotal(), (long) page, (long) size, infoPage.getRecords());
    }
}
