package com.vd.easybuy.cart_order.repository;

import java.util.List;
import java.util.Optional;

import com.vd.easybuy.cart_order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

	Optional<Order> findByOrderNumber(String orderNumber);
}
