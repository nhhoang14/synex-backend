package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.model.Cart;
import com.nhhoang.synexbackend.model.CartItem;
import com.nhhoang.synexbackend.model.Product;
import com.nhhoang.synexbackend.repository.CartItemRepository;
import com.nhhoang.synexbackend.repository.CartRepository;
import com.nhhoang.synexbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartItem addToCart(Long userId, Long productId, int quantity){

        Cart cart = cartRepository.findByUserId(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow();

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);

        return cartItemRepository.save(item);
    }

    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public Cart createCart() {
        Cart cart = new Cart();
        return cartRepository.save(cart);
    }
}