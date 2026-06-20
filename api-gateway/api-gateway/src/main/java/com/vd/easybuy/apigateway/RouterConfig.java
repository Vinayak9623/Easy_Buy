package com.vd.easybuy.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class RouterConfig {

    private final String productServiceId;
    private final String cartOrderServiceId;
    private final String userServiceId;
    private final String inventoryServiceId;

    public RouterConfig(@Value("${PRODUCT_SERVICE_ID:PRODUCT-SERVICE}") String productServiceId,
                        @Value("${CART_ORDER_SERVICE_ID:CART-ORDER-SERVICE}") String cartOrderServiceId,
                        @Value("${USER_SERVICE_ID:USER-SERVICE}") String userServiceId,
                        @Value("${INVENTORY_SERVICE_ID:INVENTORY-SERVICE}") String inventoryServiceId
    ) {
        this.productServiceId = productServiceId;
        this.cartOrderServiceId = cartOrderServiceId;
        this.userServiceId = userServiceId;
        this.inventoryServiceId = inventoryServiceId;
    }

    @Bean
    public RouteLocator route(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("product-route", r -> r
                        .path("/products/**")
                        .filters(f -> f
                                .requestRateLimiter(requestRateLimiterConfig -> requestRateLimiterConfig
                                        .setKeyResolver(keyResolver())
                                        .setRateLimiter(redisRateLimiter()))
                                .circuitBreaker(c -> c.setName("ProductCircuitBreaker")
                                        .setFallbackUri("forward:/product-fallback"))
                                .rewritePath(
                                        "/products/(?<remaining>.*)",
                                        "/${remaining}"
                                ))
                        .uri("lb://" + productServiceId))


                .route("cart-order-route", c -> c
                        .path("/cart-orders/**")
                        .filters(f -> f.rewritePath(
                                                "/cart-orders/(?<remaining>.*)",
                                                "/${remaining}"
                                        )
                                        .retry(retryConfig -> retryConfig
                                                .setRetries(3)
                                                .setMethods(HttpMethod.GET, HttpMethod.POST)
                                                .setBackoff(Duration.
                                                        ofMillis(100), Duration
                                                        .ofMillis(1000), 2, true))


                        )
                        .uri("lb://" + cartOrderServiceId))

                .route("users-route", route -> route.path("/users/**")
                        .filters(f ->
                                f.stripPrefix(1)
                        ).uri("lb://" + userServiceId))


                .route("inventory-service", route -> route.path("/inventory/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://" + inventoryServiceId))
                .build();
    }


    @Bean
    public KeyResolver keyResolver() {
        return exchange -> Mono
                .just(exchange.getRequest()
                        .getHeaders()
                        .getFirst("user"));
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(4, 4, 1);
    }

}
