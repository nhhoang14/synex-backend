package com.nhhoang.synexbackend.service.impl;

import com.nhhoang.synexbackend.entity.Media;
import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.ProductImage;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.service.MediaService;
import com.nhhoang.synexbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MediaService mediaService;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(Product product, MultipartFile mainImage, MultipartFile[] galleryImages) {
        applyUploadedImages(product, mainImage, galleryImages);
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product, MultipartFile mainImage, MultipartFile[] galleryImages, Long[] deleteImageIds) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Media oldMainMedia = existing.getMainMedia();
        List<Media> mediasToDelete = new ArrayList<>();

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setCategory(product.getCategory());
        existing.setBrand(product.getBrand());

        if (mainImage != null && !mainImage.isEmpty()) {
            existing.setMainMedia(mediaService.upload(mainImage, "synex/products"));
            if (oldMainMedia != null) {
                mediasToDelete.add(oldMainMedia);
            }
        }

        // Remove specific gallery images if requested (frontend will send their ProductImage ids)
        if (deleteImageIds != null && deleteImageIds.length > 0 && existing.getImages() != null) {
            List<ProductImage> toRemove = new ArrayList<>();
            for (ProductImage pi : existing.getImages()) {
                if (pi == null || pi.getId() == null) continue;
                for (Long delId : deleteImageIds) {
                    if (delId != null && delId.equals(pi.getId())) {
                        toRemove.add(pi);
                        break;
                    }
                }
            }

            for (ProductImage rem : toRemove) {
                existing.getImages().remove(rem);
                if (rem.getMedia() != null) {
                    mediasToDelete.add(rem.getMedia());
                }
            }
        }

        // Add newly uploaded gallery images without clearing existing ones
        if (hasAnyUploadedFile(galleryImages)) {
            if (existing.getImages() == null) {
                existing.setImages(new ArrayList<>());
            }

            for (MultipartFile file : galleryImages) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                var media = mediaService.upload(file, "synex/products/gallery");
                existing.getImages().add(new ProductImage(null, media, existing));
            }
        }

        Product saved = productRepository.saveAndFlush(existing);

        // Attempt to delete any media that are no longer used
        if (!mediasToDelete.isEmpty()) {
            mediaService.deleteIfUnused(mediasToDelete.toArray(new Media[0]));
        }

        return saved;
    }

    @Override
    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Media> mediasToDelete = new ArrayList<>();
        mediasToDelete.add(existing.getMainMedia());
        mediasToDelete.addAll(extractGalleryMedias(existing.getImages()));
        mediasToDelete.addAll(extractVariantMedias(existing.getVariants()));

        productRepository.delete(existing);
        productRepository.flush();

        mediaService.deleteIfUnused(mediasToDelete.toArray(new Media[0]));
    }

    private void applyUploadedImages(Product product, MultipartFile mainImage, MultipartFile[] galleryImages) {
        if (mainImage != null && !mainImage.isEmpty()) {
            product.setMainMedia(mediaService.upload(mainImage, "synex/products"));
        }

        if (!hasAnyUploadedFile(galleryImages)) {
            return;
        }

        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }

        product.getImages().clear();

        for (MultipartFile file : galleryImages) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            var media = mediaService.upload(file, "synex/products/gallery");
            product.getImages().add(new ProductImage(null, media, product));
        }
    }

    private boolean hasAnyUploadedFile(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return false;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private List<Media> extractGalleryMedias(List<ProductImage> images) {
        if (images == null) {
            return new ArrayList<>();
        }

        return images.stream()
                .filter(image -> image != null && image.getMedia() != null)
                .map(ProductImage::getMedia)
                .toList();
    }

    private List<Media> extractVariantMedias(List<ProductVariant> variants) {
        if (variants == null) {
            return new ArrayList<>();
        }

        return variants.stream()
                .filter(v -> v != null && v.getMedia() != null)
                .map(ProductVariant::getMedia)
                .toList();
    }
}
