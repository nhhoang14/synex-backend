package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	List<CartItem> findAllByUserId(Long userId);
	Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
	Optional<CartItem> findByUserIdAndProductIdAndVariantId(Long userId, Long productId, Long variantId);
	Optional<CartItem> findByUserIdAndProductIdAndVariantIsNull(Long userId, Long productId);
	void deleteByUserIdAndProductId(Long userId, Long productId);
	void deleteByUserIdAndProductIdAndVariantId(Long userId, Long productId, Long variantId);
	void deleteByUserId(Long userId);
}