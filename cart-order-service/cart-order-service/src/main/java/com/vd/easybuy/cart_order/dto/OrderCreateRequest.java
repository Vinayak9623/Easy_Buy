package com.vd.easybuy.cart_order.dto;

import java.util.List;

public record OrderCreateRequest(
        List<Item> items

) {
}
