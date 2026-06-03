package com.vd.easybuy.cart_order.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
		@NotBlank String shippingAddress,
		String paymentMethod) {
}
