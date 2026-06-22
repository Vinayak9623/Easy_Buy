package com.vd.easybuy.cart_order.client.fallback;

import com.vd.easybuy.cart_order.client.ProductClient;
import com.vd.easybuy.cart_order.dto.ProductSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Slf4j
public class ProductClientFallback implements ProductClient {
    @Override
    public ProductSnapshot getProductById(UUID productId) {
        log.info("Product fallback ");
        return null;
    }
}
