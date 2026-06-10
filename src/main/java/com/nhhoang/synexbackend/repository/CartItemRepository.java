package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	List<CartItem> findAllByUserId(Long userId);
	Optional<CartItem> findByUserIdAndVariantId(Long userId, Long variantId);
	void deleteById(Long id);
}