package com.vd.easybuy.cart_order.controller;
import com.vd.easybuy.cart_order.dto.CheckoutRequest;
import com.vd.easybuy.cart_order.dto.OrderResponse;
import com.vd.easybuy.cart_order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/{userId}/checkout")
	public ResponseEntity<OrderResponse> checkout(@PathVariable String userId, @Valid @RequestBody CheckoutRequest request) {
		return ResponseEntity.ok(orderService.checkout(userId, request));
	}

	@GetMapping("/{orderId}")
	public OrderResponse getOrderById(@PathVariable Long orderId) {
		return orderService.getOrderById(orderId);
	}

	@GetMapping("/number/{orderNumber}")
	public OrderResponse getOrderByNumber(@PathVariable String orderNumber) {
		return orderService.getOrderByNumber(orderNumber);
	}

	@GetMapping("/user/{userId}")
	public List<OrderResponse> getOrdersByUserId(@PathVariable String userId) {
		return orderService.getOrdersByUserId(userId);
	}

	@DeleteMapping("/{orderId}")
	public OrderResponse cancelOrder(@PathVariable Long orderId) {
		return orderService.cancelOrder(orderId);
	}
}
