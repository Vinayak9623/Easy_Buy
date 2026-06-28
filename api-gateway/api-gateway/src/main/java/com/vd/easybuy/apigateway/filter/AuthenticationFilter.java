package com.vd.easybuy.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //it should be same as per auth service
    @Value("${jwt.secret:737693658970832hgfybncybx734tr736976349nxnbx73r6763r76r79bxyr37t}")
    private String secretKey;

    public AuthenticationFilter() {
        super(Config.class);
    }

    private SecretKey getSigningKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public GatewayFilter apply(Config config) {

        //logic for token varification:

        //this is our logic for now:
        //public url --> allow
        //api/products--> GET [public]
        //api/users/login
        //api/users/ -->[POST]
        //api/privacy-policy
        //admin url --> admin role
        //api/products--> POST [admin]
        //guest/user url--> GUEST/USER role
        //api/carts -->GET [GUEST]
        //api/orders/checkout--POST [GUEST/ADMIN]


        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            logger.info("path {}", path);
            logger.info("method {}", method);

            //STEP 1: Bypass security check for public endpoints

            if (isPublicEndpoint(path, method)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try{
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String tokenUserId = String.valueOf(claims.get("userId"));
                String role=String.valueOf(claims.get("role"));

                // Ensure the user has a valid recognized role in the system

                if(!isValidRole(role)){
                    return onError(exchange,"Forbidden: Invalid User Role",HttpStatus.FORBIDDEN);
                }

                //Enforce Admin-only endpoints

                if(isAdminOnlyEndpoint(path,method) && !"ADMIN".equalsIgnoreCase(role)){
                    return onError(exchange,"Forbidden: Admin-only endpoint",HttpStatus.FORBIDDEN);
                }

                // Enforce Resource Ownership (Self-Access check for USER and GUEST)

                if(isUserOrGuest(role)){
                    String targetUserId =extractUserIdFromPath(path);

                    if(targetUserId!=null  && !targetUserId.equalsIgnoreCase(targetUserId)){
                        return onError(exchange,"Forbidden: You can not access another user data",HttpStatus.FORBIDDEN);
                    }

                }

                // STEP 7: Propagate verified user details as headers to downstream microservices
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", tokenUserId)
                        .header("X-User-Email", claims.getSubject())
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
            catch (Exception e){
                return onError(exchange,"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR);
            }

        };
    }


    private String extractUserIdFromPath(String path) {
        String[] prefixes = {"/api/carts/", "/api/orders/user/", "/api/orders/", "/api/users/"};

        for (String prefix : prefixes) {
            int index = path.indexOf(prefix);
            if (index != -1) {
                String sub = path.substring(index + prefix.length());

                // If it ends with '/checkout' (e.g. /api/orders/{userId}/checkout) remove it
                if (sub.endsWith("/checkout")) {
                    sub = sub.replace("/checkout", "");
                }

                // Extract segment before next slash if nested (e.g. /api/carts/{userId}/items)
                int slashIndex = sub.indexOf("/");
                String extractedId = (slashIndex != -1) ? sub.substring(0, slashIndex) : sub;

                // Avoid returning static endpoints as userIds
                if (extractedId.equals("login") || extractedId.equals("refresh") || extractedId.equals("change-role")) {
                    continue;
                }
                return extractedId;
            }
        }
        return null;
    }

    private boolean isUserOrGuest(String role){
        return "USER".equalsIgnoreCase(role) || "GUEST".equalsIgnoreCase(role);
    }


    private boolean isValidRole(String role){
        return "ADMIN".equalsIgnoreCase(role)||
                "GUEST".equalsIgnoreCase(role);
    }

    private boolean isAdminOnlyEndpoint(String path,String method){
        if(path.contains("/api/users/change-role")) return true;

        if(path.contains("api/users") && "GET".equalsIgnoreCase(method) &&
        !path.matches(".*/api/users/[a-fA-F0-9-]+")) return true;

        if((path.contains("/api/products") || path.contains("/api/categories") || path.contains("/api/reviews"))
        && !"GET".equalsIgnoreCase(method)) return true;

        if(path.contains("/api/inventories") && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") ||
                method.equalsIgnoreCase("DELETE")
        || method.equalsIgnoreCase("PATCH"))) return true;

        return false;
    }

    private boolean isPublicEndpoint(String path, String method) {
        return
                path.contains("/public/") ||
                        path.contains("/api/users/login") ||
                        path.contains("/api/users/refresh") ||
                        (path.contains("/api/users") && "POST".equalsIgnoreCase(method)) ||
                        (path.contains("/api/products") && "GET".equalsIgnoreCase(method)) ||
                        (path.contains("/api/categories") && "GET".equalsIgnoreCase(method)) ||
                        (path.contains("/api/reviews") && "GET".equalsIgnoreCase(method));
    }


    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.writeAndFlushWith(body -> Mono.just("Internal server Error: " + err));
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
    }
}
