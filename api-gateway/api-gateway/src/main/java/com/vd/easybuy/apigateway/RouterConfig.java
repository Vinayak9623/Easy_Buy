package com.vd.easybuy.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RouterConfig {

    private final String productServiceId;
    private final String cartOrderServiceId;

    public RouterConfig(@Value("${PRODUCT_SERVICE_ID:PRODUCT-SERVICE}") String productServiceId,
                        @Value("${CART_ORDER_SERVICE_ID:CART-ORDER-SERVICE}") String cartOrderServiceId) {
        this.productServiceId = productServiceId;
        this.cartOrderServiceId=cartOrderServiceId;
    }

    @Bean
    public RouteLocator route(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("product-route", r -> r
                        .path("/products/**")
                        .filters(f -> f.circuitBreaker(c->c.setName("ProductCircuitBreaker")
                                        .setFallbackUri("forward:/product-fallback"))
                                .rewritePath(
                                "/products/(?<remaining>.*)",
                                "/${remaining}"
                        ))
                        .uri(productServiceId))


                .route("cart-order-route",c->c
                        .path("/cart-orders/**")
                        .filters(f->f.rewritePath(
                                "/cart-orders/(?<remaining>.*)",
                                "/${remaining}"
                        ))
                        .uri(cartOrderServiceId))
                .build();
    }


    @Bean
    public RouteLocator prodroute(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("product-route", r -> r
                        .path("/products/**")
                        .filters(f -> f.rewritePath(
                                "/products/(?<remaining>.*)",
                                "/${remaining}"
                        ))
                        .uri(productServiceId))


                .route("cart-order-route",c->c
                        .path("/cart-orders/**")
                        .filters(f->f.rewritePath(
                                "/cart-orders/(?<remaining>.*)",
                                "/${remaining}"
                        ))
                        .uri(cartOrderServiceId))
                .build();
    }
}
