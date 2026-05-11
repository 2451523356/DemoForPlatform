package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.Resource;
import com.kaoyan.platform.service.ResourceService;
import com.kaoyan.platform.service.UserService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private static final String AVATAR_DIR = "C:\\Code\\DemoForPlatform\\kaoyan-platform\\backend\\upload\\avatars\\";
    private static final String RESOURCE_DIR = "C:\\Code\\DemoForPlatform\\kaoyan-platform\\backend\\upload\\files\\";
    
    private final ResourceService resourceService;
    private final UserService userService;
    
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("开始上传头像，文件大小: {}", file.getSize());
        log.info("上传目录: {}", AVATAR_DIR);
        
        if (file.isEmpty()) {
            log.error("文件为空");
            return Result.error(400, "请选择文件");
        }
        
        // 确保上传目录存在
        File uploadDir = new File(AVATAR_DIR);
        log.info("上传目录是否存在: {}", uploadDir.exists());
        
        if (!uploadDir.exists()) {
            log.info("创建上传目录");
            boolean created = uploadDir.mkdirs();
            log.info("目录创建结果: {}", created);
            if (!created) {
                log.error("目录创建失败");
                return Result.error(500, "目录创建失败");
            }
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        log.info("生成的文件名: {}", fileName);
        
        // 保存文件
        File dest = new File(AVATAR_DIR + fileName);
        log.info("文件保存路径: {}", dest.getAbsolutePath());
        
        try {
            file.transferTo(dest);
            log.info("文件保存成功");
            // 返回文件访问路径
            String fileUrl = "/avatars/" + fileName;
            log.info("返回的文件URL: {}", fileUrl);
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/resource")
    public Result<String> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("subject") String subject,
            @RequestParam("tags") String tags,
            @RequestParam("points") Integer points) {
        log.info("开始上传资源，文件大小: {}", file.getSize());
        log.info("上传目录: {}", RESOURCE_DIR);
        
        if (file.isEmpty()) {
            log.error("文件为空");
            return Result.error(400, "请选择文件");
        }
        
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 确保上传目录存在
        File uploadDir = new File(RESOURCE_DIR);
        log.info("上传目录是否存在: {}", uploadDir.exists());
        
        if (!uploadDir.exists()) {
            log.info("创建上传目录");
            boolean created = uploadDir.mkdirs();
            log.info("目录创建结果: {}", created);
            if (!created) {
                log.error("目录创建失败");
                return Result.error(500, "目录创建失败");
            }
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        log.info("生成的文件名: {}", fileName);
        
        // 保存文件
        File dest = new File(RESOURCE_DIR + fileName);
        log.info("文件保存路径: {}", dest.getAbsolutePath());
        
        try {
            file.transferTo(dest);
            log.info("文件保存成功");
            
            // 创建资源记录，状态为待审核
            Resource resource = new Resource();
            resource.setTitle(title);
            resource.setDescription(description);
            resource.setFileUrl("/files/" + fileName);
            resource.setFileType(fileExtension.substring(1));
            resource.setFileSize(file.getSize());
            resource.setCategory(category);
            resource.setSubject(subject);
            resource.setTags(tags);
            resource.setPoints(points);
            resource.setUploaderId(userId);
            resource.setDownloadCount(0);
            resource.setViewCount(0);
            resource.setRating(0.0);
            resource.setRatingCount(0);
            resource.setStatus(0); // 0-待审核，1-审核通过，2-审核拒绝
            
            resourceService.save(resource);

            // 积分奖励移至审核通过后发放（AdminController.updateResourceStatus）

            // 返回文件访问路径
            String fileUrl = "/files/" + fileName;
            log.info("返回的文件URL: {}", fileUrl);
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}