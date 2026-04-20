package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.entity.Cart;
import com.nhhoang.synexbackend.entity.CartItem;
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
                              @RequestParam(required = false) Long variantId,
                              @RequestParam int quantity) {

        return cartService.addToCart(cartId, productId, variantId, quantity);
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

    @GetMapping("/{cartId}")
    @PreAuthorize("isAuthenticated()")
    public Cart getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }
}