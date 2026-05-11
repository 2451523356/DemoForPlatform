package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.NationalScoreLine;
import com.kaoyan.platform.mapper.NationalScoreLineMapper;
import com.kaoyan.platform.service.NationalScoreLineService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NationalScoreLineServiceImpl extends ServiceImpl<NationalScoreLineMapper, NationalScoreLine>
        implements NationalScoreLineService {

    @Override
    public PageResult<NationalScoreLine> getNationalScoreLines(Integer page, Integer size, Integer year,
                                                               String subjectCategory, String degreeType, String regionType) {
        LambdaQueryWrapper<NationalScoreLine> wrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            wrapper.eq(NationalScoreLine::getYear, year);
        }
        if (StringUtils.hasText(subjectCategory)) {
            wrapper.eq(NationalScoreLine::getSubjectCategory, subjectCategory);
        }
        if (StringUtils.hasText(degreeType)) {
            wrapper.eq(NationalScoreLine::getDegreeType, degreeType);
        }
        if (StringUtils.hasText(regionType)) {
            wrapper.eq(NationalScoreLine::getRegionType, regionType);
        }
        wrapper.orderByDesc(NationalScoreLine::getYear).orderByAsc(NationalScoreLine::getSubjectCategory);

        Page<NationalScoreLine> p = page(new Page<>(page, size), wrapper);
        return new PageResult<>(p.getTotal(), (long) page, (long) size, p.getRecords());
    }
}
