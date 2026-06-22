package com.vd.easybuy.cart_order.service;

import com.vd.easybuy.cart_order.dto.AddCartItemRequest;
import com.vd.easybuy.cart_order.dto.CartResponse;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addItem(String userId, AddCartItemRequest request);

    CartResponse removeItem(String userId, String productId);

    void clearCart(String userId);
}
