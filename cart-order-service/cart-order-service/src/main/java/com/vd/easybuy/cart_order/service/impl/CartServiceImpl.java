package com.vd.easybuy.cart_order.service.impl;

import com.vd.easybuy.cart_order.client.ProductClient;
import com.vd.easybuy.cart_order.dto.*;
import com.vd.easybuy.cart_order.entity.Cart;
import com.vd.easybuy.cart_order.entity.CartItem;
import com.vd.easybuy.cart_order.entity.CartStatus;
import com.vd.easybuy.cart_order.exception.BusinessRuleException;
import com.vd.easybuy.cart_order.exception.ExternalServiceException;
import com.vd.easybuy.cart_order.exception.ResourceNotFoundException;
import com.vd.easybuy.cart_order.repository.CartRepository;
import com.vd.easybuy.cart_order.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final ProductClient productClient;

    public CartServiceImpl(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    @Override
    public CartResponse getCart(String userId) {

        Cart cart = getOrCreateActiveCart(userId);
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(String userId, AddCartItemRequest request) {

        Cart cart = getOrCreateActiveCart(userId);
        ProductSnapshot product = fetchProduct(request.productId());

        CartItem item = cart.getItems()
                .stream()
                .filter(existing -> existing.getProductId().equals(request.productId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setCart(cart);
                    created.setProductId(request.productId());
                    cart.getItems().add(created);
                    return created;
                });

        item.setProductTitle(product.title());
        item.setUnitPrice(finalUnitPrice(product.price(), product.discount()));
        item.setDiscountPercent(defaultZero(product.discount()));
        item.setQuantity(safeQuantity(item.getQuantity()) + request.quantity());

        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItem(String userId, String productId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateActiveCart(userId);
        CartItem item = findCartItem(cart, parseProductId(productId));
        item.setQuantity(request.quantity());
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse removeItem(String userId, String productId) {
        Cart cart = getOrCreateActiveCart(userId);
        CartItem item = findCartItem(cart, parseProductId(productId));
        cart.getItems().remove(item);
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public void clearCart(String userId) {
        Cart cart = getOrCreateActiveCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);

    }

    private CartItem findCartItem(Cart cart, UUID productId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found:" + productId));
    }

    private BigDecimal finalUnitPrice(Double price, Integer discount) {
        BigDecimal base = BigDecimal.valueOf(price == null ? 0.0 : price);
        BigDecimal discountFactor = BigDecimal.valueOf(100 - defaultZero(discount))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
    }

    private Cart getOrCreateActiveCart(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessRuleException("User is required");
        }

        return cartRepository.findByUserIdAndStatus(normlizeUserId(userId), CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(normlizeUserId(userId));
                    cart.setStatus(CartStatus.ACTIVE);
                    cart.setItems(new ArrayList<>());
                    return cartRepository.save(cart);
                });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream().map(this::toItemResponse).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(),
                cart.getUserId(),
                cart.getStatus(),
                total,
                items,
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                cart.getCheckedOutAt());
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getDiscountPercent(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    private String normlizeUserId(String userId) {
        return userId.trim();
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }

    private UUID parseProductId(String productId) {
        try {
            return UUID.fromString(productId);
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Invalid product: " + productId);
        }
    }

    public ProductSnapshot fetchProduct(UUID productId) {
        try {
            ProductSnapshot product = productClient.getProductById(productId);
            if (product == null || Boolean.FALSE.equals(product.live())) {
                throw new BusinessRuleException("Product is not available");
            }
            return product;
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to load product" + productId, ex);
        }
    }
}
