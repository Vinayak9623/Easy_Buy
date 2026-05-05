package com.vd.easybuy.products.service.impl;

import com.vd.easybuy.products.dto.ProductDto;
import com.vd.easybuy.products.dto.ReviewDto;
import com.vd.easybuy.products.entity.Product;
import com.vd.easybuy.products.entity.Review;
import com.vd.easybuy.products.repository.ProductRepo;
import com.vd.easybuy.products.repository.ReviewRepo;
import com.vd.easybuy.products.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    public final ReviewRepo reviewRepo;
    public final ProductRepo productRepo;

    public ReviewServiceImpl(ReviewRepo reviewRepo, ProductRepo productRepo) {
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
    }


    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDto getReviewById(Long reviewId) {
        return toDto(findReview(reviewId));
    }

    @Override
    public List<ReviewDto> getReviewsByProductId(UUID productId) {
        return reviewRepo.findByProduct_Id(productId)
                .stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ReviewDto createReview(UUID productId, ReviewDto reviewDto) {
        Product product = findProduct(productId);
        Review review = new Review();
        review.setId(reviewDto.getId());
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setProduct(product);
        return toDto(reviewRepo.save(review));
    }

    @Override
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto) {
        Review review = findReview(reviewId);
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        return toDto(reviewRepo.save(review));
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = findReview(reviewId);
        reviewRepo.delete(review);
    }


    private Product findProduct(UUID productId) {
        return productRepo.findById(productId).orElseThrow(
                () -> new RuntimeException("Product not found Exception")
        );
    }

    private Review findReview(Long reviewId) {
        return reviewRepo.findById(reviewId).
                orElseThrow(() -> new RuntimeException("Review not found"));
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();

        dto.setId(review.getId());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());

        if (review.getProduct() != null) {
            dto.setProduct(toProductDto(review.getProduct()));
        }
        return dto;
    }

    private ProductDto toProductDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setShortDesc(product.getShortDesc());
        dto.setLongDesc(product.getLongDesc());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setLive(product.getLive());
        dto.setProductImages(product.getProductImages() == null ?
                new ArrayList<>() : new ArrayList<>(product.getProductImages()));
        dto.setCategories(new ArrayList<>());
        dto.setReviews(new ArrayList<>());
        return dto;
    }
}
