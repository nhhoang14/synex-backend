package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> getAllProducts();

    Optional<Product> getProductById(Long id);

    Product createProduct(Product product, MultipartFile mainImage, MultipartFile[] galleryImages);

    Product updateProduct(Long id, Product product, MultipartFile mainImage, MultipartFile[] galleryImages, Long[] deleteImageIds);

    void deleteProduct(Long id);
}