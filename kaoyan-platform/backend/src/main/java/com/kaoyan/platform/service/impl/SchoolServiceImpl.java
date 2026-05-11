package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.School;
import com.kaoyan.platform.entity.SchoolScoreLine;
import com.kaoyan.platform.mapper.SchoolMapper;
import com.kaoyan.platform.mapper.SchoolScoreLineMapper;
import com.kaoyan.platform.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {

    private final SchoolScoreLineMapper schoolScoreLineMapper;

    @Override
    public PageResult<School> getSchoolList(Integer page, Integer size, String type, String province, String keyword) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getStatus, 1);

        if (type != null && !type.isEmpty()) {
            wrapper.eq(School::getType, type);
        }

        if (province != null && !province.isEmpty()) {
            wrapper.eq(School::getProvince, province);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(School::getName, keyword);
        }

        wrapper.orderByAsc(School::getRanking);

        Page<School> schoolPage = new Page<>(page, size);
        schoolPage = baseMapper.selectPage(schoolPage, wrapper);

        return new PageResult<>(schoolPage.getTotal(), (long) page, (long) size, schoolPage.getRecords());
    }

    @Override
    public School getSchoolDetail(Long id) {
        School school = baseMapper.selectById(id);
        if (school == null) {
            throw new RuntimeException("学校不存在");
        }
        return school;
    }

    @Override
    public PageResult<SchoolScoreLine> getScoreLines(Long schoolId, Integer year, String major, Integer page, Integer size) {
        LambdaQueryWrapper<SchoolScoreLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolScoreLine::getSchoolId, schoolId);

        if (year != null) {
            wrapper.eq(SchoolScoreLine::getYear, year);
        }

        if (major != null && !major.isEmpty()) {
            wrapper.like(SchoolScoreLine::getMajor, major);
        }

        wrapper.orderByDesc(SchoolScoreLine::getYear).orderByAsc(SchoolScoreLine::getMajor);

        Page<SchoolScoreLine> scoreLinePage = new Page<>(page, size);
        scoreLinePage = schoolScoreLineMapper.selectPage(scoreLinePage, wrapper);

        // Join school name into score line results
        School school = baseMapper.selectById(schoolId);
        if (school != null) {
            for (SchoolScoreLine scoreLine : scoreLinePage.getRecords()) {
                scoreLine.setSchoolName(school.getName());
            }
        }

        return new PageResult<>(scoreLinePage.getTotal(), (long) page, (long) size, scoreLinePage.getRecords());
    }
}
