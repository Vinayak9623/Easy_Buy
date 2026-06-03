package com.vd.easybuy.cart_order.repository;

import java.util.Optional;
import java.util.UUID;

import com.vd.easybuy.cart_order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	Optional<CartItem> findByCartIdAndProductId(Long cartId, UUID productId);
}
