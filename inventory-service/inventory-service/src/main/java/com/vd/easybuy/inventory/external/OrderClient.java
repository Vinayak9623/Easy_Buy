package com.vd.easybuy.inventory.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name="${CART_ORDER_SERVICE_ID: CART-ORDER-SERVICE}")
public interface OrderClient {

    @GetMapping("/api/orders/{orderId}")
    OrderResponse getOrderById(@PathVariable("orderId") Long orderId);
}
