package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Resource;
import com.kaoyan.platform.mapper.ResourceMapper;
import com.kaoyan.platform.service.NotificationPushService;
import com.kaoyan.platform.service.ResourceService;
import com.kaoyan.platform.service.UserBehaviorLogService;
import com.kaoyan.platform.service.UserService;
import com.kaoyan.platform.service.DownloadHistoryService;
import com.kaoyan.platform.service.PointsRecordService;
import com.kaoyan.platform.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Autowired
    private UserBehaviorLogService userBehaviorLogService;
    
    @Autowired
    private NotificationPushService notificationPushService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DownloadHistoryService downloadHistoryService;
    
    @Autowired
    private PointsRecordService pointsRecordService;
    
    @Override
    public PageResult<Resource> getResourceList(Integer page, Integer size, String category, String subject, String keyword, String sortBy) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1);

        if (category != null && !category.isEmpty()) {
            wrapper.eq(Resource::getCategory, category);
        }

        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(Resource::getSubject, subject);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Resource::getTitle, keyword).or().like(Resource::getDescription, keyword));
        }

        // 排序：默认按创建时间倒序
        if ("downloads".equals(sortBy)) {
            wrapper.orderByDesc(Resource::getDownloadCount);
        } else if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Resource::getRating);
        } else {
            wrapper.orderByDesc(Resource::getCreateTime);
        }

        Page<Resource> resourcePage = page(new Page<>(page, size), wrapper);

        return new PageResult<>(resourcePage.getTotal(), (long) page, (long) size, resourcePage.getRecords());
    }
    
    @Override
    public Resource getResourceById(Long id) {
        Resource resource = getById(id);
        if (resource != null) {
            // 记录用户查看资源的行为
            Long userId = UserContext.getUserId();
            if (userId != null) {
                recordResourceView(userId, id);
            }
        }
        return resource;
    }
    
    @Override
    public boolean incrementDownloadCount(Long id) {
        Resource resource = getById(id);
        if (resource != null) {
            Long userId = UserContext.getUserId();
            if (userId != null) {
                // 检查是否已经下载过（避免重复扣积分）
                boolean alreadyDownloaded = downloadHistoryService.hasDownloaded(userId, id);
                if (alreadyDownloaded) {
                    // 已下载过，不扣积分，直接允许下载
                    resource.setDownloadCount(resource.getDownloadCount() + 1);
                    updateById(resource);
                    return true;
                }

                // 检查资源是否需要积分
                if (resource.getPoints() > 0) {
                    // 扣减积分
                    boolean deducted = userService.deductPoints(
                            userId,
                            resource.getPoints(),
                            "下载资源：" + resource.getTitle(),
                            resource.getId(),
                            "resource"
                    );
                    if (!deducted) {
                        return false; // 积分不足
                    }
                }

                // 记录下载历史
                com.kaoyan.platform.entity.DownloadHistory history = new com.kaoyan.platform.entity.DownloadHistory();
                history.setUserId(userId);
                history.setResourceId(resource.getId());
                history.setResourceTitle(resource.getTitle());
                history.setResourceCategory(resource.getCategory());
                history.setDownloadTime(java.time.LocalDateTime.now());
                history.setPointsConsumed(resource.getPoints());
                downloadHistoryService.addDownloadHistory(history);

                // 记录用户下载资源的行为
                recordResourceDownload(userId, id);
            }

            // 增加下载计数
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            updateById(resource);
            return true;
        }
        return false;
    }
    
    @Override
    public void incrementViewCount(Long id) {
        Resource resource = getById(id);
        if (resource != null) {
            resource.setViewCount(resource.getViewCount() + 1);
            updateById(resource);
        }
    }

    @Override
    public void recordResourceDownload(Long userId, Long resourceId) {
        userBehaviorLogService.recordBehavior(userId, "download_resource", "resource", resourceId);
    }

    @Override
    public void recordResourceView(Long userId, Long resourceId) {
        userBehaviorLogService.recordBehavior(userId, "view_resource", "resource", resourceId);
    }
    
    @Override
    public boolean save(Resource resource) {
        boolean saved = super.save(resource);
        if (saved && resource.getStatus() == 1) {
            notificationPushService.pushResourceUpdateNotification(
                    resource.getId(),
                    resource.getTitle(),
                    resource.getCategory()
            );
        }
        return saved;
    }
    
    @Override
    public boolean updateById(Resource resource) {
        boolean updated = super.updateById(resource);
        if (updated && resource.getStatus() == 1) {
            notificationPushService.pushResourceUpdateNotification(
                    resource.getId(),
                    resource.getTitle(),
                    resource.getCategory()
            );
        }
        return updated;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取项目根目录
        String projectDir = System.getProperty("user.dir");
        String uploadDir = projectDir + "/upload/files";

        // 创建上传目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.lastIndexOf('.') > 0 
                ? originalFilename.substring(originalFilename.lastIndexOf('.')) 
                : "";
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // 保存文件
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        // 返回相对路径
        return "/files/" + fileName;
    }

    @Override
    public void addResource(Resource resource) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        // 设置上传者ID
        resource.setUploaderId(userId);
        // 设置初始值
        resource.setDownloadCount(0);
        resource.setViewCount(0);
        resource.setRating(0.0);
        resource.setRatingCount(0);
        resource.setStatus(0); // 默认为待审核，管理员审核通过后设为1
        resource.setCreateTime(LocalDateTime.now());
        resource.setUpdateTime(LocalDateTime.now());
        resource.setDeleted(0);

        // 保存资源（save方法内已包含推送通知逻辑）
        save(resource);
    }
}
