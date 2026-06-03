package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    boolean existsByMediaId(Long mediaId);
}