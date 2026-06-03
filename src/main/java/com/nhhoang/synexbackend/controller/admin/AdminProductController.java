package com.nhhoang.synexbackend.controller.admin;

import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin("*")
public class AdminProductController {

    private final ProductService productService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> create(@RequestPart("product") Product product,
                                          @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
                                          @RequestPart(value = "galleryImages", required = false) MultipartFile[] galleryImages) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product, mainImage, galleryImages));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @RequestPart("product") Product product,
                                          @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
                                          @RequestPart(value = "galleryImages", required = false) MultipartFile[] galleryImages,
                                          @RequestParam(value = "deleteImageIds", required = false) Long[] deleteImageIds) {
        return ResponseEntity.ok(productService.updateProduct(id, product, mainImage, galleryImages, deleteImageIds));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}