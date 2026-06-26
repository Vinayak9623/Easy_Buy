package com.vd.easybuy.notification.consumer;

import com.vd.easybuy.common.events.PaymentEvent;
import com.vd.easybuy.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventConsumer {

    private final EmailService emailService;
    private static final String PAYMENT_TOPIC="payment-topic";

    public PaymentEventConsumer(EmailService emailService){
        this.emailService=emailService;
    }

    @KafkaListener(topics = PAYMENT_TOPIC,groupId = "notification-group")
    public void consumePaymentEvent(PaymentEvent paymentEvent){

        log.info("Consuming PaymentEvent: {}", paymentEvent);

        try{
            if("PAID".equalsIgnoreCase(paymentEvent.getStatus())){
                emailService.sendOrderConfirmationEmail(paymentEvent);

                log.info("Order confirmation email sent successfully for Order ID: {}", paymentEvent.getOrderId());
            }
            else {
                log.warn("Payment failed for order: {} "+paymentEvent.getOrderId());
            }

        }catch (Exception e){

            log.error("Failed to send email for Order : {}",
                    paymentEvent.getOrderId(), e);
        }

    }

}
