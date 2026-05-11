package com.kaoyan.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件上传路径映射
        // 使用绝对路径确保无论服务器从哪个目录启动都能正确找到文件
        String basePath = "C:\\Code\\DemoForPlatform\\kaoyan-platform\\backend\\";
        
        // 将 /files/** 映射到本地文件系统的 upload 目录
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + basePath + "upload/");
        
        // 配置头像上传路径映射
        // 将 /avatars/** 映射到本地文件系统的 upload/avatars 目录
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + basePath + "upload/avatars/");
        
        // 配置视频路径映射
        // 将 /videos/** 映射到本地文件系统的 upload/videos 目录
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + basePath + "upload/videos/");
    }
}
