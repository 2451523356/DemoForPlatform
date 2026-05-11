package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.DownloadHistory;
import com.kaoyan.platform.entity.Resource;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.service.DownloadHistoryService;
import com.kaoyan.platform.service.ResourceService;
import com.kaoyan.platform.service.UserService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final UserService userService;
    private final DownloadHistoryService downloadHistoryService;

    @GetMapping("/list")
    public Result<PageResult<Resource>> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy) {
        return Result.success(resourceService.getResourceList(page, size, category, subject, keyword, sortBy));
    }

    @GetMapping("/detail/{id}")
    public Result<Resource> getResourceDetail(@PathVariable Long id) {
        resourceService.incrementViewCount(id);
        return Result.success(resourceService.getResourceById(id));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable Long id) {
        Resource resource = resourceService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        boolean success = resourceService.incrementDownloadCount(id);
        if (!success) {
            return ResponseEntity.status(403).build();
        }

        String fileUrl = resource.getFileUrl();
        if (fileUrl == null || fileUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 外部URL：生成示例文件（避免网络依赖）
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            String content = "【考研学习平台 - 资源文件】\n\n"
                    + "资源名称：" + resource.getTitle() + "\n"
                    + "资源分类：" + (resource.getCategory() != null ? resource.getCategory() : "") + "\n"
                    + "所属科目：" + (resource.getSubject() != null ? resource.getSubject() : "") + "\n"
                    + "资源描述：" + (resource.getDescription() != null ? resource.getDescription() : "") + "\n\n"
                    + "--- 示例文件 ---\n"
                    + "实际部署时此处为真实资源文件，可通过后台管理系统上传替换。\n";
            byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            String fileName = resource.getTitle() + ".txt";
            String encodedName;
            try { encodedName = java.net.URLEncoder.encode(fileName, "UTF-8"); }
            catch (java.io.UnsupportedEncodingException e) { encodedName = "resource.txt"; }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(new org.springframework.core.io.ByteArrayResource(bytes));
        }

        // 本地文件
        File file = resolveFile(fileUrl);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            encodedFileName = file.getName();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<FileSystemResource> previewResource(@PathVariable Long id) {
        Resource resource = resourceService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        String fileUrl = resource.getFileUrl();
        if (fileUrl == null || fileUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = resolveFile(fileUrl);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = getMediaType(file.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }

    private File resolveFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return null;

        String fileName = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
        fileName = fileName.replace("files/", "");

        String projectDir = System.getProperty("user.dir");
        // 按优先级依次尝试多个路径
        String[] candidates = {
            projectDir + "/upload/files/" + fileName,
            projectDir + "/kaoyan-platform/backend/upload/files/" + fileName,
            projectDir + "/../upload/files/" + fileName,
            projectDir + "/../kaoyan-platform/backend/upload/files/" + fileName
        };
        for (String path : candidates) {
            File file = new File(path);
            if (file.exists()) return file;
        }
        return null;
    }

    private MediaType getMediaType(String fileName) {
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0 && dot < fileName.length() - 1) {
            ext = fileName.substring(dot + 1).toLowerCase();
        }
        switch (ext) {
            case "pdf": return MediaType.APPLICATION_PDF;
            case "mp4": case "webm": case "ogg":
                return MediaType.valueOf("video/" + ext);
            case "jpg": case "jpeg": return MediaType.IMAGE_JPEG;
            case "png": return MediaType.IMAGE_PNG;
            case "gif": return MediaType.IMAGE_GIF;
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @PostMapping("/upload")
    public Result<java.util.Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = resourceService.uploadFile(file);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileSize", file.getSize());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> addResource(@RequestBody Resource resource) {
        try {
            resourceService.addResource(resource);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "资源添加失败: " + e.getMessage());
        }
    }
}
