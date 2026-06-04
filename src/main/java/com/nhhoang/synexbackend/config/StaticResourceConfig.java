package com.nhhoang.synexbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final String uploadUrlPrefix;

    public StaticResourceConfig(@Value("${app.upload.dir:uploads}") String uploadDir,
                                @Value("${app.upload.url-prefix:/uploads}") String uploadUrlPrefix) {
        this.uploadDir = uploadDir;
        this.uploadUrlPrefix = normalizeUrlPrefix(uploadUrlPrefix);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path location = Paths.get(uploadDir).toAbsolutePath().normalize();
        String resourceLocation = location.toUri().toString();
        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations(resourceLocation);
    }

    private String normalizeUrlPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/uploads";
        }

        String normalized = prefix.trim().replace('\\', '/');
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}