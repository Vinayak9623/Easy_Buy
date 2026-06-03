package com.vd.easybuy.cart_order.repository;

import com.vd.easybuy.cart_order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
