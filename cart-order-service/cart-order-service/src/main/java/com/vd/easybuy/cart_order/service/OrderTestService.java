package com.vd.easybuy.cart_order.service;

import com.vd.easybuy.cart_order.dto.OrderCreateRequest;
import com.vd.easybuy.cart_order.dto.ProductResponse;

public interface OrderTestService {

    ProductResponse createOrder(OrderCreateRequest orderCreateRequest);
    ProductResponse createOrderWithRestClient(OrderCreateRequest orderCreateRequest);
    ProductResponse createOrderWithFeign(OrderCreateRequest orderCreateRequest);
}
