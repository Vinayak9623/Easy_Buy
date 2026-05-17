package com.vd.easybuy.products.service;

import com.vd.easybuy.products.dto.PageResponse;
import com.vd.easybuy.products.dto.ProductDto;
import com.vd.easybuy.products.dto.ReviewDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    PageResponse<ProductDto> getAllProducts(int page,int size);
    ProductDto getProductById(UUID productId);
    PageResponse<ProductDto> getProductsByCategoryId(Long categoryId,int page,int size);
    ProductDto createProduct(ProductDto productDto);
    ProductDto updateProduct(UUID productId,ProductDto productDto);
    void deleteProduct(UUID productId);
    ProductDto addCategoryToProduct(UUID productId,Long categoryId);
    ProductDto removeCategoryFromProduct(UUID productId,Long categoryId);
    ReviewDto addReviewToProduct(UUID productId,ReviewDto reviewDto);
    ProductDto addProductImages(UUID productId, List<MultipartFile> files);
    List<String> getProductImages(UUID productId);
}
