package com.vd.easybuy.cart_order.service.impl;

import com.vd.easybuy.cart_order.client.ProductClient;
import com.vd.easybuy.cart_order.dto.AddCartItemRequest;
import com.vd.easybuy.cart_order.dto.CartItemResponse;
import com.vd.easybuy.cart_order.dto.CartResponse;
import com.vd.easybuy.cart_order.entity.Cart;
import com.vd.easybuy.cart_order.entity.CartItem;
import com.vd.easybuy.cart_order.entity.CartStatus;
import com.vd.easybuy.cart_order.exception.BusinessRuleException;
import com.vd.easybuy.cart_order.repository.CartRepository;
import com.vd.easybuy.cart_order.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final ProductClient productClient;

    public CartServiceImpl(CartRepository cartRepository,ProductClient productClient){
        this.cartRepository=cartRepository;
        this.productClient=productClient;
    }

    @Override
    public CartResponse getCart(String userId) {

        Cart cart =getOrCreateActiveCart(userId);
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(String userId, AddCartItemRequest request) {
        return null;
    }

    @Override
    public CartResponse removeItem(String userId, String productId) {
        return null;
    }

    @Override
    public void clearCart(String userId) {

    }

    private Cart getOrCreateActiveCart(String userId){
        if(!StringUtils.hasText(userId)){
            throw new BusinessRuleException("User is required");
        }

        return cartRepository.findByUserIdAndStatus(normlizeUserId(userId), CartStatus.ACTIVE)
                .orElseGet(()->{
                    Cart cart=new Cart();
                    cart.setUserId(normlizeUserId(userId));
                    cart.setStatus(CartStatus.ACTIVE);
                    cart.setItems(new ArrayList<>());
                    return cartRepository.save(cart);
                });
    }

    private CartResponse toResponse(Cart cart){
        List<CartItemResponse>  items =cart.getItems()
                .stream().map(this::toItemResponse).toList();

        items.stream()
                .map(Cart)
    }

    private CartItemResponse toItemResponse(CartItem item){
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
    private String normlizeUserId(String userId){
        return userId.trim();
    }
}
