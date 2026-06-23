package com.vd.easybuy.cart_order.dto;

import com.vd.easybuy.cart_order.entity.OrderStatus;
import com.vd.easybuy.cart_order.entity.PaymentMethod;
import com.vd.easybuy.cart_order.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String billingName,
        String billingPhone,
        String orderNumber,
        String userId,
        String shippingAddress,
        PaymentStatus paymentStatus,
        String extraInformation,
        PaymentMethod paymentMethod,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt,
        Instant cancelledAt) {
}
