package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.User;
import com.nhhoang.synexbackend.entity.Wishlist;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import com.nhhoang.synexbackend.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void toggleWishlist(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .ifPresentOrElse(
                        wishlistRepository::delete,
                        () -> {
                            Wishlist wishlist = new Wishlist();
                            wishlist.setUser(user);
                            wishlist.setProduct(product);
                            wishlistRepository.save(wishlist);
                        }
                );
    }

    public List<Product> getMyWishlist() {
        User user = getCurrentUser();
        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(Wishlist::getProduct)
                .toList();
    }

    public boolean isInWishlist(Long productId) {
        User user = getCurrentUser();
        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }
}
