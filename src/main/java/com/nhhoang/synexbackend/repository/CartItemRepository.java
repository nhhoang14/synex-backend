package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
	Optional<CartItem> findByCartIdAndProductIdAndVariantId(Long cartId, Long productId, Long variantId);
	Optional<CartItem> findByCartIdAndProductIdAndVariantIsNull(Long cartId, Long productId);
	void deleteByCartIdAndProductId(Long cartId, Long productId);
	void deleteByCartIdAndProductIdAndVariantId(Long cartId, Long productId, Long variantId);
	void deleteByCartId(Long cartId);
}