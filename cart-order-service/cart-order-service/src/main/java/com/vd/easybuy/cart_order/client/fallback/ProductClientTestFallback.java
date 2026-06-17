package com.vd.easybuy.cart_order.client.fallback;

import com.vd.easybuy.cart_order.client.ProductClientTest;
import com.vd.easybuy.cart_order.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ProductClientTestFallback implements ProductClientTest {
    @Override
    public ProductResponse getPeoductById(String productId) {
        log.info("product service is down");
        return null;
    }
}
