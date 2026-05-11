package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.NationalScoreLine;
import com.kaoyan.platform.entity.School;
import com.kaoyan.platform.entity.SchoolScoreLine;
import com.kaoyan.platform.service.NationalScoreLineService;
import com.kaoyan.platform.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;
    private final NationalScoreLineService nationalScoreLineService;

    @GetMapping("/list")
    public Result<PageResult<School>> getSchoolList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String keyword) {
        return Result.success(schoolService.getSchoolList(page, size, type, province, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<School> getSchoolDetail(@PathVariable Long id) {
        return Result.success(schoolService.getSchoolDetail(id));
    }

    @GetMapping("/{id}/score-lines")
    public Result<PageResult<SchoolScoreLine>> getScoreLines(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(schoolService.getScoreLines(id, year, major, page, size));
    }

    @GetMapping("/national-score")
    public Result<PageResult<NationalScoreLine>> getNationalScoreLines(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String subjectCategory,
            @RequestParam(required = false) String degreeType,
            @RequestParam(required = false) String regionType) {
        return Result.success(nationalScoreLineService.getNationalScoreLines(page, size, year, subjectCategory, degreeType, regionType));
    }
}
