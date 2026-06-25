package com.vd.easybuy.payments.service;

import com.vd.easybuy.payments.dto.PaymentRequest;
import com.vd.easybuy.payments.dto.Paymentresponse;

import java.util.List;

public interface PaymentService {

    Paymentresponse processPayment(PaymentRequest request);
    List<Paymentresponse> getPaymentByOrderId(Long orderId);
    Paymentresponse getPaymentByTransactionId(String transactionId);
}
