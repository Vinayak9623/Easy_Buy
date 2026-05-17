package com.vd.easybuy.products.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private UUID id;
    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "shortDesc is required")
    @Size(max = 500, message = "shortDesc must be less than 500 characters")
    private String shortDesc;
    @NotBlank(message = "longdesc is required")
    private String longDesc;

    @NotNull(message = "price is required")
    @Positive(message = "price must be positive")
    private Double price;

    @Min(value = 0, message = "discount must be positive")
    @Max(value = 100, message = "discount must be less than 100")
    private Integer discount;
    private Boolean live;
    private List<String> productImages;
    private List<CategoryDto> categories;
    private List<ReviewDto> reviews;
}
