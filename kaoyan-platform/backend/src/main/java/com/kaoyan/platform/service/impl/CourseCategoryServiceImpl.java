package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.CourseCategory;
import com.kaoyan.platform.mapper.CourseCategoryMapper;
import com.kaoyan.platform.service.CourseCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {
    
    @Override
    public List<CourseCategory> getCategoryTree() {
        List<CourseCategory> allCategories = list(new LambdaQueryWrapper<CourseCategory>()
                .eq(CourseCategory::getStatus, 1)
                .orderByAsc(CourseCategory::getSort)
                .orderByDesc(CourseCategory::getId));
        return allCategories;
    }
    
    @Override
    public List<CourseCategory> getAdminCategoryTree() {
        return list(new LambdaQueryWrapper<CourseCategory>()
                .orderByAsc(CourseCategory::getSort)
                .orderByDesc(CourseCategory::getId));
    }

    @Override
    public List<CourseCategory> getCategoriesByParentId(Long parentId) {
        return list(new LambdaQueryWrapper<CourseCategory>()
                .eq(CourseCategory::getParentId, parentId)
                .eq(CourseCategory::getStatus, 1)
                .orderByAsc(CourseCategory::getSort)
                .orderByDesc(CourseCategory::getId));
    }
}
