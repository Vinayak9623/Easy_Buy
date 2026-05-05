package com.vd.easybuy.products.controller;

import com.vd.easybuy.products.dto.ReviewDto;
import com.vd.easybuy.products.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/{reviewId}")
    ResponseEntity<ReviewDto> getReviewById(@PathVariable("reviewId") Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @GetMapping("/product/{productId}")
    ResponseEntity<List<ReviewDto>> getReviewsByProductId(@PathVariable("productId") UUID productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }

    @PostMapping("/product/{productId}")
    ResponseEntity<ReviewDto> createReview(@PathVariable("productId") UUID productId, @RequestBody ReviewDto reviewDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(productId, reviewDto));
    }

    @PutMapping("/{reviewId}")
    ResponseEntity<ReviewDto> updateReview(@PathVariable("reviewId") Long reviewId, @RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, reviewDto));
    }

    @DeleteMapping("/{reviewId}")
    ResponseEntity<?> deleteReview(@PathVariable("reviewId") Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
