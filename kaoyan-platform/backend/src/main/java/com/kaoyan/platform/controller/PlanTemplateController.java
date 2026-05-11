package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.PlanTemplate;
import com.kaoyan.platform.service.PlanTemplateService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plan-template")
@RequiredArgsConstructor
public class PlanTemplateController {

    private final PlanTemplateService planTemplateService;

    @GetMapping("/list")
    public Result<List<PlanTemplate>> getList() {
        return Result.success(planTemplateService.getList());
    }

    @GetMapping("/detail/{id}")
    public Result<PlanTemplate> getDetail(@PathVariable Long id) {
        return Result.success(planTemplateService.getDetail(id));
    }

    @PostMapping("/apply/{id}")
    public Result<Map<String, Object>> applyTemplate(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return Result.success(planTemplateService.applyTemplate(id, userId));
    }
}
