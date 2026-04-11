package com.nhhoang.synexbackend.controller;

import com.nhhoang.synexbackend.model.Cart;
import com.nhhoang.synexbackend.model.CartItem;
import com.nhhoang.synexbackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public CartItem addToCart(@RequestParam(required = false) Long cartId,
                              @RequestParam Long productId,
                              @RequestParam int quantity) {

        return cartService.addToCart(cartId, productId, quantity);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Cart createCart() {
        return cartService.createCart();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Cart getCurrentUserCart() {
        return cartService.getCurrentUserCart();
    }

    @PatchMapping("/items/{productId}/increase")
    @PreAuthorize("isAuthenticated()")
    public CartItem increaseItemQuantity(@PathVariable Long productId,
                                         @RequestParam(defaultValue = "1") int amount) {
        return cartService.increaseItemQuantity(productId, amount);
    }

    @PatchMapping("/items/{productId}/decrease")
    @PreAuthorize("isAuthenticated()")
    public CartItem decreaseItemQuantity(@PathVariable Long productId,
                                         @RequestParam(defaultValue = "1") int amount) {
        return cartService.decreaseItemQuantity(productId, amount);
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public void removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
    }

    @GetMapping("/{cartId}")
    @PreAuthorize("isAuthenticated()")
    public Cart getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }
}