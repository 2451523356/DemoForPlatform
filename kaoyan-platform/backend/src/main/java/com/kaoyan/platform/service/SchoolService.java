package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.School;
import com.kaoyan.platform.entity.SchoolScoreLine;

public interface SchoolService extends IService<School> {

    PageResult<School> getSchoolList(Integer page, Integer size, String type, String province, String keyword);

    School getSchoolDetail(Long id);

    PageResult<SchoolScoreLine> getScoreLines(Long schoolId, Integer year, String major, Integer page, Integer size);
}
