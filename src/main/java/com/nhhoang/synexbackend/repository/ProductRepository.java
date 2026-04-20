package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}