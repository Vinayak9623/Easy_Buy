package com.vd.easybuy.cart_order.controller;

import com.vd.easybuy.cart_order.dto.AddCartItemRequest;
import com.vd.easybuy.cart_order.dto.CartResponse;
import com.vd.easybuy.cart_order.dto.UpdateCartItemRequest;
import com.vd.easybuy.cart_order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService){
        this.cartService=cartService;
    }

    @GetMapping("/{userId}")
    public CartResponse  getCart(@PathVariable String userId){
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem( @PathVariable String userId, @Valid @RequestBody AddCartItemRequest request){
        return ResponseEntity.ok(cartService.addItem(userId,request));
    }

    @PutMapping("/{userId}/items/{productId}")
    public CartResponse updateItem(@PathVariable String userId, @PathVariable String productId,  @Valid @RequestBody UpdateCartItemRequest request){
        return cartService.updateItem(userId,productId,request);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public CartResponse removeItem(@PathVariable String userId, @PathVariable String productId){
        return cartService.removeItem(userId,productId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable String userId){
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
