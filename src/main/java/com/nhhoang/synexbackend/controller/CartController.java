package com.nhhoang.synexbackend.controller;

import com.nhhoang.synexbackend.model.Cart;
import com.nhhoang.synexbackend.model.CartItem;
import com.nhhoang.synexbackend.model.Product;
import com.nhhoang.synexbackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public CartItem addToCart(@RequestParam Long cartId,
                              @RequestParam Long productId,
                              @RequestParam int quantity) {

        return cartService.addToCart(cartId, productId, quantity);
    }

    @PostMapping
    public Cart createCart() {
        return cartService.createCart();
    }

    @GetMapping("/{cartId}")
    public Cart getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }
}