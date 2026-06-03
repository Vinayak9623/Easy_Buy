package com.vd.easybuy.cart_order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSnapshot(
        UUID id,
        String title,
        String shortDesc,
        String longDesc,
        Double price,
        Integer discount,
        Boolean live

) {
}
