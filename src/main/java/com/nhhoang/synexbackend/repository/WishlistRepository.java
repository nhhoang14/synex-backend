package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
	void deleteByUserId(Long userId);
}