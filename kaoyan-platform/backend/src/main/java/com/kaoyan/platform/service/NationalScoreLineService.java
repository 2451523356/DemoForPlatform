package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.NationalScoreLine;

public interface NationalScoreLineService extends IService<NationalScoreLine> {

    PageResult<NationalScoreLine> getNationalScoreLines(Integer page, Integer size, Integer year,
                                                        String subjectCategory, String degreeType, String regionType);
}
