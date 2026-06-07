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

    public RouterConfig(@Value("${product.service.id}") String productServiceId,
                        @Value("${cart-order.service.id}") String cartOrderServiceId) {
        this.productServiceId = productServiceId;
        this.cartOrderServiceId=cartOrderServiceId;
    }

    @Bean
    @Profile("dev")
    public RouteLocator route(RouteLocatorBuilder builder) {

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


    @Profile("prod")
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
