package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.entity.PlanTemplate;

import java.util.List;
import java.util.Map;

public interface PlanTemplateService extends IService<PlanTemplate> {

    List<PlanTemplate> getList();

    PlanTemplate getDetail(Long id);

    Map<String, Object> applyTemplate(Long id, Long userId);
}
