package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@CrossOrigin("*")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}/toggle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggle(@PathVariable Long productId) {
        wishlistService.toggleWishlist(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Product>> getMyWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @GetMapping("/{productId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> getStatus(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isInWishlist(productId));
    }
}