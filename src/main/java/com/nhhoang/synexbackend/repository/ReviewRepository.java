package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	void deleteByUserId(Long userId);
}