package com.nhhoang.synexbackend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalImageUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads}")
    private String uploadUrlPrefix;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        try {
            Files.createDirectories(this.rootLocation);

            String originalName = file.getOriginalFilename();
            String extension = extractExtension(originalName);
            String fileName = UUID.randomUUID() + extension;

            Path destination = this.rootLocation.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return normalizePrefix(uploadUrlPrefix) + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to upload image", ex);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String prefix = normalizePrefix(uploadUrlPrefix) + "/";
            
            if (imageUrl.contains(prefix)) {
                String fileName = imageUrl.substring(imageUrl.indexOf(prefix) + prefix.length());
                
                Path filePath = this.rootLocation.resolve(fileName).normalize();
                
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    System.out.println("Successfully deleted orphaned file: " + fileName);
                } else {
                    System.out.println("File not found on disk, skipped deletion: " + fileName);
                }
            }
        } catch (IOException ex) {
            System.err.println("Failed to delete old image file from disk: " + imageUrl + " -> " + ex.getMessage());
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex);
    }

    private String normalizePrefix(String prefix) {
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