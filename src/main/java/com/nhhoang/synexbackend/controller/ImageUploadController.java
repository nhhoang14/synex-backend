package com.nhhoang.synexbackend.controller;

import com.nhhoang.synexbackend.dto.response.ImageUploadResponse;
import com.nhhoang.synexbackend.service.LocalImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ImageUploadController {

    private final LocalImageUploadService localImageUploadService;

    @PostMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file,
                                                           @RequestParam("purpose") String purpose,
                                                           Authentication authentication) {
        UploadPurpose uploadPurpose = UploadPurpose.from(purpose);
        validatePermission(uploadPurpose, authentication);

        String url = localImageUploadService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadResponse(url));
    }

    private void validatePermission(UploadPurpose purpose, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (purpose.isAdminOnly() && !isAdmin) {
            throw new AccessDeniedException("Only admin can upload images for this purpose");
        }
    }

    private enum UploadPurpose {
        CATEGORY_IMAGE,
        PRODUCT_IMAGE,
        PRODUCT_VARIANT_IMAGE,
        CONTACT_MESSAGE_IMAGE;

        static UploadPurpose from(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                throw new IllegalArgumentException("Upload purpose is required");
            }

            String normalized = rawValue.trim().toUpperCase();
            try {
                return UploadPurpose.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Invalid purpose. Supported values: CATEGORY_IMAGE, PRODUCT_IMAGE, PRODUCT_VARIANT_IMAGE, CONTACT_MESSAGE_IMAGE"
                );
            }
        }

        boolean isAdminOnly() {
            return this == CATEGORY_IMAGE || this == PRODUCT_IMAGE || this == PRODUCT_VARIANT_IMAGE;
        }
    }
}