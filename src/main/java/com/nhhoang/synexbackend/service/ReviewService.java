package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.CreateReviewRequest;
import com.nhhoang.synexbackend.dto.request.UpdateReviewRequest;
import com.nhhoang.synexbackend.dto.response.ReviewResponse;
import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.Review;
import com.nhhoang.synexbackend.entity.User;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.ReviewRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getByProductId(Long productId) {
        ensureProductExists(productId);
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getCurrentUserReviews() {
        User currentUser = getCurrentUser();
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ReviewResponse create(CreateReviewRequest request) {
        validateCreateRequest(request);

        User currentUser = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (reviewRepository.existsByUserIdAndProductId(currentUser.getId(), product.getId())) {
            throw new RuntimeException("You already reviewed this product");
        }

        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));
        review.setUser(currentUser);
        review.setProduct(product);

        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse updateMyReview(Long reviewId, UpdateReviewRequest request) {
        validateUpdateRequest(request);

        User currentUser = getCurrentUser();
        Review review = reviewRepository.findByIdAndUserId(reviewId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));

        return toResponse(reviewRepository.save(review));
    }

    public void deleteMyReview(Long reviewId) {
        User currentUser = getCurrentUser();
        Review review = reviewRepository.findByIdAndUserId(reviewId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Review not found"));
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAll() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void adminDelete(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        reviewRepository.delete(review);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
    }

    private void validateCreateRequest(CreateReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Review request is required");
        }

        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required");
        }

        validateRatingAndComment(request.getRating(), request.getComment());
    }

    private void validateUpdateRequest(UpdateReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Review request is required");
        }

        validateRatingAndComment(request.getRating(), request.getComment());
    }

    private void validateRatingAndComment(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment is required");
        }
    }

    private String normalizeComment(String comment) {
        return comment == null ? null : comment.trim();
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getProduct() == null ? null : review.getProduct().getId(),
                review.getProduct() == null ? null : review.getProduct().getName(),
                review.getUser() == null ? null : review.getUser().getId(),
                review.getUser() == null ? null : review.getUser().getUsername(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
