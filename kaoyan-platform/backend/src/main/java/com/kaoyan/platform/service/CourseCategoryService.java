package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.entity.CourseCategory;

import java.util.List;

public interface CourseCategoryService extends IService<CourseCategory> {
    
    List<CourseCategory> getCategoryTree();
    
    List<CourseCategory> getCategoriesByParentId(Long parentId);

    List<CourseCategory> getAdminCategoryTree();
}
