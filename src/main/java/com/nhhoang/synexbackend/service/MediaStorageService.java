package com.nhhoang.synexbackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    MediaUploadResult uploadImage(MultipartFile file, String folder);

    void deleteImage(String publicId);

    String extractPublicId(String imageUrl);
}