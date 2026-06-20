package com.vd.easybuy.inventory.external;

import com.vd.easybuy.common.payload.ProductSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "${PRODUCT_SERVICE_ID}")
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    ProductSnapshot getProductById(@PathVariable UUID productId);
}
