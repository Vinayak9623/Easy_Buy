package com.vd.easybuy.cart_order.repository;

import java.util.Optional;

import com.vd.easybuy.cart_order.entity.Cart;
import com.vd.easybuy.cart_order.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

	Optional<Cart> findByUserIdAndStatus(String userId, CartStatus status);
}
