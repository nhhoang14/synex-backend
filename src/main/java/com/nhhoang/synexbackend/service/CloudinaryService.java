package com.nhhoang.synexbackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResult uploadImage(MultipartFile file, String folder);

    void deleteImage(String publicId);

    String extractPublicId(String imageUrl);
}