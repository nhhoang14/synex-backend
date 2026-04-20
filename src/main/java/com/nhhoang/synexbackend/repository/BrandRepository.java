package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}