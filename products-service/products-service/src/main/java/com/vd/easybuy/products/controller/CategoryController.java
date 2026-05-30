package com.vd.easybuy.products.controller;

import com.vd.easybuy.products.dto.CategoryDto;
import com.vd.easybuy.products.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService=categoryService;
    }


    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable("categoryId") Long categoryId){
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CategoryDto>> getCategoryByProductId(@PathVariable("productId") UUID productId){
        return ResponseEntity.ok(categoryService.getCategoryByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryDto));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable("categoryId") Long categoryId,@Valid @RequestBody CategoryDto categoryDto){
          return ResponseEntity.ok(categoryService.updateCategory(categoryId,categoryDto));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory( @PathVariable("categoryId") Long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
