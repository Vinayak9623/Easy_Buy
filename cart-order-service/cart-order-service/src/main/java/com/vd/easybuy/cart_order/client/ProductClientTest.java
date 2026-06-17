package com.vd.easybuy.cart_order.client;

import com.vd.easybuy.cart_order.client.fallback.ProductClientTestFallback;
import com.vd.easybuy.cart_order.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "${PRODUCT_SERVICE_ID}")
@FeignClient(name = "PRODUCT-SERVICE",fallback = ProductClientTestFallback.class)
public interface ProductClientTest {

    @GetMapping("/api/products/{productId}")
    ProductResponse getPeoductById(@PathVariable("productId") String productId);
}
