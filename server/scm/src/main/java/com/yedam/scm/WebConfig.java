package com.yedam.scm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.employee-dir}")
    private String employeeUploadDir;

    @Value("${file.upload.defect-dir}")
    private String defectUploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // URL 예시: http://localhost:8080/uploads/employee/EMP001.jpg
        registry.addResourceHandler("/uploads/employee/**")
                .addResourceLocations("file:///" + employeeUploadDir + "/");
        
        registry.addResourceHandler("/uploads/defect/**")
                .addResourceLocations("file:///" + defectUploadDir + "/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{spring:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**")
                .setViewName("forward:/index.html");
    }


    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}