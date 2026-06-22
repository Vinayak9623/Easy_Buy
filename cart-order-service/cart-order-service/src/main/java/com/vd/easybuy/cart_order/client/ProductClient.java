package com.vd.easybuy.cart_order.client;

import com.vd.easybuy.cart_order.client.fallback.ProductClientFallback;
import com.vd.easybuy.cart_order.dto.ProductSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "${PRODUCT_SERVICE_ID}",url = "${PRODUCT_SERVICE_URL:}",fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    ProductSnapshot getProductById(@PathVariable("productId")UUID productId);
}
