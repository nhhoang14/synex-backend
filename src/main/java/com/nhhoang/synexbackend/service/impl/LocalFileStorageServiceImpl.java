package com.nhhoang.synexbackend.service.impl;

import com.nhhoang.synexbackend.service.MediaStorageService;
import com.nhhoang.synexbackend.service.MediaUploadResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements MediaStorageService {

    private final Path storageRoot;
    private final String urlPrefix;

    public LocalFileStorageServiceImpl(@Value("${app.upload.dir:uploads}") String uploadDir,
                                       @Value("${app.upload.url-prefix:/uploads}") String urlPrefix) {
        this.storageRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.urlPrefix = normalizeUrlPrefix(urlPrefix);

        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize upload directory", ex);
        }
    }

    @Override
    public MediaUploadResult uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String normalizedFolder = normalizeFolder(folder);
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);

        Path relativePath = normalizedFolder.isBlank()
                ? Paths.get(fileName)
                : Paths.get(normalizedFolder, fileName);
        Path targetPath = storageRoot.resolve(relativePath).normalize();

        if (!targetPath.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid upload path");
        }

        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String publicId = normalizePathSeparators(relativePath.toString());
            String url = buildPublicUrl(publicId);

            return new MediaUploadResult(url, publicId);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store image file", ex);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        String resolvedPublicId = normalizePublicId(publicId);
        if (resolvedPublicId == null) {
            return;
        }

        Path filePath = storageRoot.resolve(resolvedPublicId).normalize();
        if (!filePath.startsWith(storageRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete local image file", ex);
        }
    }

    @Override
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        String path = imageUrl.trim();
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                path = URI.create(path).getPath();
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }

        if (path == null || path.isBlank()) {
            return null;
        }

        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        String prefixWithSlash = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";

        if (normalizedPath.startsWith(prefixWithSlash)) {
            return normalizePublicId(normalizedPath.substring(prefixWithSlash.length()));
        }

        return normalizePublicId(path);
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "";
        }

        String normalized = normalizePathSeparators(folder.trim());
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Invalid folder path");
        }

        return normalized;
    }

    private String normalizePublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }

        String normalized = normalizePathSeparators(publicId.trim());
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isBlank() || normalized.contains("..")) {
            return null;
        }

        return normalized;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        String normalized = fileName.trim();
        int lastDot = normalized.lastIndexOf('.');
        if (lastDot < 0 || lastDot == normalized.length() - 1) {
            return "";
        }

        return normalized.substring(lastDot + 1).toLowerCase();
    }

    private String buildPublicUrl(String publicId) {
        String relative = publicId.startsWith("/") ? publicId.substring(1) : publicId;
        String relativePath = normalizePathSeparators(relative);

        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(urlPrefix)
                    .path("/")
                    .path(relativePath)
                    .toUriString();
        } catch (IllegalStateException ex) {
            return urlPrefix + "/" + relativePath;
        }
    }

    private String normalizeUrlPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/uploads";
        }

        String normalized = normalizePathSeparators(prefix.trim());
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String normalizePathSeparators(String value) {
        return value.replace('\\', '/');
    }
}