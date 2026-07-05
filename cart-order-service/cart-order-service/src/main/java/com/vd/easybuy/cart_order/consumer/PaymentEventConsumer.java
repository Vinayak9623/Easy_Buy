package com.vd.easybuy.cart_order.consumer;

import com.vd.easybuy.cart_order.service.OrderService;
import com.vd.easybuy.common.events.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentEventConsumer {

    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    public void consumePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Received PaymentEvent from Kafka: {}", paymentEvent);

        if(paymentEvent.getOrderId()==null){
            log.error("Received payment with null order id");
            return;
        }

        try{
            log.info("Updating order payment status from kafka for order ID: {} with status: {}",
                    paymentEvent.getOrderId(), paymentEvent.getStatus());
            orderService.updatePaymentStatus(paymentEvent.getOrderId(), paymentEvent.getStatus());
        }
        catch (Exception e){
            log.error("Failed to update order payment status from kafka for order ID: {} with status: {}",
                    paymentEvent.getOrderId(), paymentEvent.getStatus(), e);
        }

    }
}
