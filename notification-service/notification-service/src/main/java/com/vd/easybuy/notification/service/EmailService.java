package com.vd.easybuy.notification.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.vd.easybuy.common.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Resend resend;

    public void sendOrderConfirmationEmail(PaymentEvent event){

        String html = """
                <h2>🎉 Order Confirmed</h2>

                <p>Hello Vinayak,</p>

                <p>Your payment has been received successfully.</p>

                <hr>

                <p><b>Order Id :</b> %d</p>

                <p><b>Transaction Id :</b> %s</p>

                <p><b>Amount :</b> ₹ %s</p>

                <p><b>Status :</b> %s</p>

                <hr>

                <p>Thank you for shopping with EasyBuy ❤️</p>

                """.formatted(
                event.getOrderId(),
                event.getTransactionId(),
                event.getAmount(),
                event.getStatus()
        );

        CreateEmailOptions param = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to("umeshbichukale03@gmail.com")
                .subject("EasyBuy - Order Confirmation")
                .html(html)
                .build();

        try{

            resend.emails().send(param);
            log.info("Email sent successfully.");
        }
        catch (Exception e){
            log.error("Failed to send email for Order : {}", event.getOrderId(), e);
            throw new RuntimeException(e);
        }

    }
}
