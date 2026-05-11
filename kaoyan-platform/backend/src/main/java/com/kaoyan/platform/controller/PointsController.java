package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.service.PointsRecordService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {
    
    private final PointsRecordService pointsRecordService;
    
    @GetMapping("/records")
    public Result<?> getPointsRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(pointsRecordService.getUserPointsRecords(userId, page, size));
    }
    
    @GetMapping("/summary")
    public Result<?> getPointsSummary() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        int totalIncome = pointsRecordService.getTotalPointsByType(userId, "income");
        int totalExpense = pointsRecordService.getTotalPointsByType(userId, "expense");
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("balance", totalIncome + totalExpense);
        return Result.success(result);
    }
}
