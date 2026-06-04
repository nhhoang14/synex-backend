package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.entity.CartItem;
import com.nhhoang.synexbackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public CartItem addToCart(@RequestParam Long productId,
                              @RequestParam(required = false) Long variantId,
                              @RequestParam int quantity) {

        return cartService.addToCart(productId, variantId, quantity);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<CartItem> getCurrentUserCart() {
        return cartService.getCurrentUserCart();
    }

    @PatchMapping("/items/{productId}/increase")
    @PreAuthorize("isAuthenticated()")
    public CartItem increaseItemQuantity(@PathVariable Long productId,
                                         @RequestParam(required = false) Long variantId,
                                         @RequestParam(defaultValue = "1") int amount) {
        return cartService.increaseItemQuantity(productId, variantId, amount);
    }

    @PatchMapping("/items/{productId}/decrease")
    @PreAuthorize("isAuthenticated()")
    public CartItem decreaseItemQuantity(@PathVariable Long productId,
                                         @RequestParam(required = false) Long variantId,
                                         @RequestParam(defaultValue = "1") int amount) {
        return cartService.decreaseItemQuantity(productId, variantId, amount);
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public void removeFromCart(@PathVariable Long productId,
                               @RequestParam(required = false) Long variantId) {
        cartService.removeFromCart(productId, variantId);
    }
}