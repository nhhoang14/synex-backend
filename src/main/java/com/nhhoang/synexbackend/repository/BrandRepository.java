package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}