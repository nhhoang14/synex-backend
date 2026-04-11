package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
	void deleteByCartId(Long cartId);
}