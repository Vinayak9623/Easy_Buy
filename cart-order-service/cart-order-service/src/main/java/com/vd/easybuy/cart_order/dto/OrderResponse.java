package com.vd.easybuy.cart_order.dto;

import com.vd.easybuy.cart_order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
		Long id,
		String orderNumber,
		String userId,
		String shippingAddress,
		String paymentMethod,
		OrderStatus status,
		BigDecimal totalAmount,
		List<OrderItemResponse> items,
		Instant createdAt,
		Instant updatedAt,
		Instant cancelledAt) {
}
