package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}
