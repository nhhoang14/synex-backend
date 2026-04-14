package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}
