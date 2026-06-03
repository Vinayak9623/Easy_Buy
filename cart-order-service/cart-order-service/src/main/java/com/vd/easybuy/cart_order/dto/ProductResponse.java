package com.vd.easybuy.cart_order.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductResponse {

    private String id;


    private String title;


    private String shortDesc;


    private String longDesc;


    private Double price;


    private Integer discount;

    private Boolean live;
    private List<String> productImages;
}
