package com.nhhoang.synexbackend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nhhoang.synexbackend.service.CloudinaryService;
import com.nhhoang.synexbackend.service.CloudinaryUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Pattern CLOUDINARY_PUBLIC_ID_PATTERN = Pattern.compile("/upload/(?:v\\d+/)?(.+?)(?:\\.[^.]+)?$");

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            Object publicId = uploadResult.get("public_id");
            if (secureUrl == null) {
                throw new IllegalStateException("Failed to upload image to Cloudinary");
            }
            if (publicId == null) {
                throw new IllegalStateException("Failed to resolve Cloudinary public id");
            }

            return new CloudinaryUploadResult(secureUrl.toString(), publicId.toString());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read image file", ex);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete image from Cloudinary", ex);
        }
    }

    @Override
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {
            String path = URI.create(imageUrl).getPath();
            if (path == null || path.isBlank()) {
                return null;
            }

            Matcher matcher = CLOUDINARY_PUBLIC_ID_PATTERN.matcher(path);
            if (!matcher.find()) {
                return null;
            }

            return matcher.group(1);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}