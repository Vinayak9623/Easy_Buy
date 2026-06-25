package com.vd.easybuy.payments.dto;

import com.vd.easybuy.payments.entity.PaymentMethod;
import com.vd.easybuy.payments.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record Paymentresponse(
        Long id,
        String transactionId,
        Long orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String paymentGatewayTxnId,
        Instant createdAt,
        Instant updateAdt
) {

}
