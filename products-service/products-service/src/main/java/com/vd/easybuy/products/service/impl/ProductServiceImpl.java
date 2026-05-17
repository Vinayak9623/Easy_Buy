package com.vd.easybuy.products.service.impl;

import com.vd.easybuy.products.dto.CategoryDto;
import com.vd.easybuy.products.dto.PageResponse;
import com.vd.easybuy.products.dto.ProductDto;
import com.vd.easybuy.products.dto.ReviewDto;
import com.vd.easybuy.products.entity.Category;
import com.vd.easybuy.products.entity.Product;
import com.vd.easybuy.products.entity.Review;
import com.vd.easybuy.products.repository.CategoryRepo;
import com.vd.easybuy.products.repository.ProductRepo;
import com.vd.easybuy.products.repository.ReviewRepo;
import com.vd.easybuy.products.service.ImageStorageService;
import com.vd.easybuy.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ReviewRepo reviewRepo;
    private final ImageStorageService imageStorageService;

    @Override
    public PageResponse<ProductDto> getAllProducts(int page, int size) {
        PageRequest request = PageRequest.of(page, size);
        Page<Product> products = productRepo.findAll(request);
        return toPagedResponse(products.map(this::toDto));
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        return toDto(findProduct(productId));
    }

    @Override
    public PageResponse<ProductDto> getProductsByCategoryId(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepo.findByCategories_Id(categoryId, pageable);
        return toPagedResponse(productPage.map(this::toDto));
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = new Product();
        applyBesicField(product, productDto);
        List<Category> categories =
                resolveCategories(productDto.getCategories());
        product.setCategories(categories);
        Product save = productRepo.save(product);
        syncCategoryLinks(save, categories);
        return toDto(save);
    }

    @Override
    public ProductDto updateProduct(UUID productId, ProductDto productDto) {
        Product product = findProduct(productId);
        applyBesicField(product, productDto);
        if (productDto.getCategories() != null) {
            List<Category> categories = resolveCategories(productDto.getCategories());
            product.setCategories(categories);
            //update product with category
            Product savedProduct = productRepo.save(product);
            //syncing..
            syncCategoryLinks(savedProduct, categories);
            //product --> product dto
            return toDto(savedProduct);
        }
        return toDto(productRepo.save(product));
    }

    @Override
    public void deleteProduct(UUID productId) {
        Product product = findProduct(productId);
        productRepo.delete(product);
    }

    @Override
    public ProductDto addCategoryToProduct(UUID productId, Long categoryId) {
        Product product = findProduct(productId);
        Category category = findCategory(categoryId);
        if (!product.getCategories().contains(category)) {
            product.getCategories().add(category);
        }
        if (!category.getProducts().contains(product)) {
            category.getProducts().add(product);
        }
        categoryRepo.save(category);
        return toDto(productRepo.save(product));
    }

    @Override
    public ProductDto removeCategoryFromProduct(UUID productId, Long categoryId) {
        Product product = findProduct(productId);
        Category category = findCategory(categoryId);

        //1step
        product.getCategories().remove(category);
        //2step
        category.getProducts().remove(product);

        categoryRepo.save(category);
        return toDto(productRepo.save(product));
    }

    @Override
    public ReviewDto addReviewToProduct(UUID productId, ReviewDto reviewDto) {
        Product product = findProduct(productId);
        Review review = new Review();
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setProduct(product);
        return toReviewDto(reviewRepo.save(review));
    }

    @Override
    public ProductDto addProductImages(UUID productId, List<MultipartFile> files) {
        //        fetch product
        Product product = findProduct(productId);

        //will upload the images:
        List<String> uploadedUrls = uploadImages(files);


        if (product.getProductImages() == null) {
            product.setProductImages(new ArrayList<>());
        }
        product.getProductImages().addAll(uploadedUrls);
        return toDto(productRepo.save(product));
    }

    @Override
    public List<String> getProductImages(UUID productId) {
        return List.of();
    }

    private List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("At least one product image is required");
        }
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedUrls.add(imageStorageService.upload(file));
        }
        return uploadedUrls;
    }
    private ReviewDto toReviewDto(Review review) {
        ReviewDto dto = toReviewDtoShallow(review);
        if (review.getProduct() != null) {
            dto.setProduct(toProductDtoShallow(review.getProduct()));
        }
        return dto;
    }

    private ProductDto toProductDtoShallow(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setShortDesc(product.getShortDesc());
        dto.setLongDesc(product.getLongDesc());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setLive(product.getLive());
        dto.setProductImages(product.getProductImages() == null ? new ArrayList<>() : new ArrayList<>(product.getProductImages()));
        dto.setCategories(new ArrayList<>());
        dto.setReviews(new ArrayList<>());
        return dto;
    }

    private void syncCategoryLinks(Product product, List<Category> categories) {
        for (Category category : categories) {
            if (!category.getProducts().contains(product)) {
                category.getProducts().add(product);
            }
            categoryRepo.save(category);
        }

    }

    private List<Category> resolveCategories(List<CategoryDto> categoryDtos) {
        if (categoryDtos == null) {
            return new ArrayList<>();
        }
        List<Category> categories = new ArrayList<>();
        for (CategoryDto categoryDto : categoryDtos) {
            if (categoryDto.getId() == null) {
                Category category = new Category();
                category.setTitle(categoryDto.getTitle());
                categories.add(categoryRepo.save(category));
            } else {
                categories.add(findCategory(categoryDto.getId()));
            }
        }
        return categories;
    }

    private Category findCategory(Long categoryId) {
        return categoryRepo.findById(categoryId).orElseThrow(()
                -> new RuntimeException
                ("Category not found with given id" + categoryId));
    }

    private void applyBesicField(Product product, ProductDto productDto) {

        product.setTitle(productDto.getTitle());
        product.setShortDesc(productDto.getShortDesc());
        product.setLongDesc(productDto.getLongDesc());
        product.setPrice(productDto.getPrice());
        product.setDiscount(productDto.getDiscount());

        if (productDto.getLive() != null) {
            product.setLive(productDto.getLive());
        }

        if (productDto.getProductImages() != null) {
            product.setProductImages(new ArrayList<>(productDto.getProductImages()));
        }
    }


    private Product findProduct(UUID productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new
                        RuntimeException(
                        "Product not found with given Id"
                                + productId));
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        List<Integer> list = new ArrayList<>(10);

        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setShortDesc(product.getShortDesc());
        dto.setLongDesc(product.getLongDesc());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setLive(product.getLive());
        dto.setProductImages(product.getProductImages() == null ?
                new ArrayList<>() : new ArrayList<>(product.getProductImages()));

        dto.setCategories(product.getCategories() == null ?
                new ArrayList<>() : product.getCategories().stream()
                .map(this::toCategoryDtoShallow).collect(Collectors.toList()));

        dto.setReviews(product.getReviews() == null ? new ArrayList<>()
                : product.getReviews().stream().map(this::toReviewDtoShallow)
                .collect(Collectors.toList()));

        return dto;

    }

    private ReviewDto toReviewDtoShallow(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setProduct(null);
        return dto;
    }

    private CategoryDto toCategoryDtoShallow(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setTitle(category.getTitle());
        dto.setProducts(new ArrayList<>());
        return dto;
    }

    private PageResponse<ProductDto> toPagedResponse(Page<ProductDto> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast()
        );
    }
}
