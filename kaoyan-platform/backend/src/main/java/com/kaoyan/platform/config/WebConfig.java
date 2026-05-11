package com.kaoyan.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = "C:\\Code\\DemoForPlatform\\kaoyan-platform\\backend\\";
        
        // 配置视频文件的访问路径
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + basePath + "upload/videos/");
        
        // 配置头像文件的访问路径
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + basePath + "upload/avatars/");
        
        // 配置其他静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}