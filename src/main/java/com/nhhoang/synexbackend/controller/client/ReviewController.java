package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.dto.request.CreateReviewRequest;
import com.nhhoang.synexbackend.dto.request.UpdateReviewRequest;
import com.nhhoang.synexbackend.dto.response.ReviewResponse;
import com.nhhoang.synexbackend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getByProductId(productId));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewResponse>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getCurrentUserReviews());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> create(@RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(request));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long reviewId,
                                                 @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateMyReview(reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId) {
        reviewService.deleteMyReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
