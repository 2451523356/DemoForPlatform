package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Resource;

public interface ResourceService extends IService<Resource> {
    
    PageResult<Resource> getResourceList(Integer page, Integer size, String category, String subject, String keyword, String sortBy);
    
    Resource getResourceById(Long id);
    
    boolean incrementDownloadCount(Long id);
    
    void incrementViewCount(Long id);

    void recordResourceDownload(Long userId, Long resourceId);

    void recordResourceView(Long userId, Long resourceId);
    
    String uploadFile(org.springframework.web.multipart.MultipartFile file);
    
    void addResource(Resource resource);
}
