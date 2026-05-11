package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.AdjustmentInfo;
import com.kaoyan.platform.service.AdjustmentInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adjustment")
@RequiredArgsConstructor
public class AdjustmentInfoController {

    private final AdjustmentInfoService adjustmentInfoService;

    @GetMapping("/list")
    public Result<PageResult<AdjustmentInfo>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) String major) {
        return Result.success(adjustmentInfoService.getList(page, size, year, schoolName, major));
    }
}
