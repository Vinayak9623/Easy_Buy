package com.vd.easybuy.cart_order.service.impl;

import com.vd.easybuy.cart_order.client.InventoryClient;
import com.vd.easybuy.cart_order.client.ProductClient;
import com.vd.easybuy.cart_order.dto.*;
import com.vd.easybuy.cart_order.entity.*;
import com.vd.easybuy.cart_order.exception.BusinessRuleException;
import com.vd.easybuy.cart_order.exception.ExternalServiceException;
import com.vd.easybuy.cart_order.exception.ResourceNotFoundException;
import com.vd.easybuy.cart_order.producer.OrderEventPublisher;
import com.vd.easybuy.cart_order.repository.CartRepository;
import com.vd.easybuy.cart_order.repository.OrderRepository;
import com.vd.easybuy.cart_order.service.OrderService;
import com.vd.easybuy.common.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final ProductClient productClient;
    private final OrderEventPublisher orderEventPublisher;


    //1. Get Active Cart
//2. Validate Cart
//3. Reserve Inventory
//4. Build Order
//5. Save Order
//6. Mark Cart Checked Out
//7. Publish Order Event
//8. Return Response

    @Override
    public OrderResponse checkout(String userId, CheckoutRequest request) {

        Cart cart = cartRepository.findByUserIdAndStatus(normalizeUserId(userId), CartStatus.ACTIVE)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active cart not found for userId: " + userId));

        if(cart.getItems().isEmpty()){
            throw new BusinessRuleException("Cart is empty");
        }

        List<InventorySnapshot> reservedSnapshots=new ArrayList<>();

        try{

            for(CartItem item:cart.getItems()){
                reservedSnapshots.add(inventoryClient
                        .reserveByProductId(item.getProductId(),new ReserveStockRequest(item.getQuantity())));
            }

            Order order =buildOrderFromCart(cart,request);

            Order saved = orderRepository.save(order);

            cart.setStatus(CartStatus.CHECKED_OUT);
            cart.setCheckedOutAt(Instant.now());
            cart.getItems().clear();
            cartRepository.save(cart);

            //order event publish

            OrderEvent orderEvent=new OrderEvent();
            orderEvent.setOrderId(saved.getId());
            orderEvent.setUserId(saved.getUserId());
            orderEvent.setStatus(saved.getStatus().toString());
            orderEvent.setMessage("Order created successfully");
            orderEvent.setTotalAmount(saved.getTotalAmount());
            orderEventPublisher.publishOrderCreatedEvent(orderEvent);

            return toResponse(saved);
        }catch (RuntimeException ex) {
            for (int i = reservedSnapshots.size() - 1; i >= 0; i--) {
                CartItem item = cart.getItems().get(i);
                try {
                    inventoryClient.releaseByProductId(item.getProductId(), new ReleaseStockRequest(item.getQuantity()));
                } catch (Exception releaseEx) {
                    throw new ExternalServiceException("Checkout failed and stock rollback also failed for productId: " + item.getProductId(), releaseEx);
                }
            }
            if (ex instanceof ExternalServiceException externalServiceException) {
                throw externalServiceException;
            }
            throw new ExternalServiceException("Checkout failed", ex);
        }
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        return toResponse(orderRepository.findById(orderId)
                .orElseThrow(()->
                        new ResourceNotFoundException("order not fount with id: "+orderId)));
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber) {
        return toResponse(orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for orderNumber: " + orderNumber)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(normalizeUserId(userId))
                .stream().map(this::toResponse).toList();

    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toResponse(order);
        }
        for (OrderItem item : order.getItems()) {
            try {
                inventoryClient.releaseByProductId(item.getProductId(), new ReleaseStockRequest(item.getQuantity()));
            } catch (Exception ex) {
                throw new ExternalServiceException("Failed to release stock for productId: " + item.getProductId(), ex);
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public void releaseReservedStock(UUID productId, Integer quantity) {
        try {
            inventoryClient.releaseByProductId(productId, new ReleaseStockRequest(quantity));
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to release stock for productId: " + productId, ex);
        }
    }

    @Override
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        log.info("Updating payment status for Order ID: {} to {}", orderId, paymentStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));
        order.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));
        orderRepository.save(order);
        log.info("Payment status successfully updated for Order ID: {}", orderId);
    }

    private String normalizeUserId(String userId){

        if(userId==null || userId.isBlank()){
            throw new BusinessRuleException("User id is required");
        }
        return userId.trim();
    }

     private Order buildOrderFromCart(Cart cart,CheckoutRequest request){

        Order order =new Order();

        order.setOrderNumber(UUID.randomUUID().toString());
        order.setUserId(cart.getUserId());
        order.setBillingName(request.billingName().trim());
        order.setBillingPhone(request.billingPhone().trim());
        order.setExtraInformation(request.extraInformation().trim());
        order.setShippingAddress(request.shippingAddress().trim());
        order.setPaymentMethod(request.paymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setItems(new ArrayList<>());

         BigDecimal total=BigDecimal.ZERO;


         for(CartItem cartItem:cart.getItems()){

             OrderItem orderItem =new OrderItem();
             orderItem.setOrder(order);
             orderItem.setProductId(cartItem.getProductId());
             orderItem.setProductTitle(cartItem.getProductTitle());
             orderItem.setUnitPrice(cartItem.getUnitPrice());
             orderItem.setDiscountPercent(cartItem.getDiscountPercent());
             orderItem.setQuantity(cartItem.getQuantity());
             orderItem.setLineTotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())).setScale(2, RoundingMode.HALF_UP));
             order.getItems().add(orderItem);

             total=total.add(orderItem.getLineTotal());
         }

         order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

         return order;

     }


    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getBillingName(),
                order.getBillingPhone(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getShippingAddress(),
                order.getPaymentStatus(),
                order.getExtraInformation(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCancelledAt());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getDiscountPercent(),
                item.getQuantity(),
                item.getLineTotal());
    }


}