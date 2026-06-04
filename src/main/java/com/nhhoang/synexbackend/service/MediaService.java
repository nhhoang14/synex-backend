package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.entity.Media;
import com.nhhoang.synexbackend.repository.MediaRepository;
import com.nhhoang.synexbackend.repository.ProductImageRepository;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final MediaStorageService mediaStorageService;

    public Media upload(MultipartFile file, String folder) {
        MediaUploadResult uploadResult = mediaStorageService.uploadImage(file, folder);
        return mediaRepository.save(new Media(null, uploadResult.url(), uploadResult.publicId()));
    }

    public Media resolveFromUrl(String imageUrl) {
        String publicId = mediaStorageService.extractPublicId(imageUrl);
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("Image URL is invalid");
        }

        return mediaRepository.findByPublicId(publicId)
                .orElseGet(() -> mediaRepository.save(new Media(null, imageUrl, publicId)));
    }

    public void deleteIfUnused(Media... medias) {
        if (medias == null || medias.length == 0) {
            return;
        }

        Set<Long> handledMediaIds = new HashSet<>();
        for (Media media : medias) {
            deleteIfUnusedInternal(media, handledMediaIds);
        }
    }

    private void deleteIfUnusedInternal(Media media, Set<Long> handledMediaIds) {
        if (media == null || media.getId() == null || !handledMediaIds.add(media.getId())) {
            return;
        }

        if (productRepository.existsByMainMediaId(media.getId())
                || productImageRepository.existsByMediaId(media.getId())
                || productVariantRepository.existsByMediaId(media.getId())) {
            return;
        }

        mediaStorageService.deleteImage(media.getPublicId());
        mediaRepository.delete(media);
    }
}