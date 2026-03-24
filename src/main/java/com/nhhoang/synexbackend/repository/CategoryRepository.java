package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}