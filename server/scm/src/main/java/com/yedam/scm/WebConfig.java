package com.yedam.scm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.employee-dir}")
    private String employeeUploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // URL 예시: http://localhost:8080/uploads/employee/EMP001.jpg
        registry.addResourceHandler("/uploads/employee/**")
                .addResourceLocations("file:///" + employeeUploadDir + "/");
    }
}