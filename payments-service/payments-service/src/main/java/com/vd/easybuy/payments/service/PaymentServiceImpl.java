package com.vd.easybuy.payments.service;

import com.vd.easybuy.payments.dto.PaymentRequest;
import com.vd.easybuy.payments.dto.Paymentresponse;
import com.vd.easybuy.payments.entity.PaymentStatus;
import com.vd.easybuy.payments.entity.Transaction;
import com.vd.easybuy.payments.exception.BusinessRuleException;
import com.vd.easybuy.payments.exception.ResourceNotfoundException;
import com.vd.easybuy.payments.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService{

    private final TransactionRepository transactionRepository;

    public PaymentServiceImpl(TransactionRepository transactionRepository){
        this.transactionRepository=transactionRepository;
    }

    @Override
    public Paymentresponse processPayment(PaymentRequest request) {

        log.info("Processing payment for Order Id: {} with amount: {}",request.orderId(),request.amount());

        if(request.paymentDetails()==null || request.paymentDetails().trim().isEmpty()){
            throw new BusinessRuleException("payment details (card/wallet info) are required");
        }

        Transaction transaction =new Transaction();

        transaction.setOrderId(request.orderId());
        transaction.setAmount(request.amount());
        transaction.setPaymentMethod(request.paymentMethod());
        transaction.setTransactionId(UUID.randomUUID().toString());


        String details=request.paymentDetails().toLowerCase();
        if(details.contains("fail")||details.contains("1111-1111-1111-1111")||
        details.contains("error")){
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setPaymentGatewayTxnId("GAteway-fail"+UUID.randomUUID().toString().substring(0,8).toUpperCase());
            Transaction saved=transactionRepository.save(transaction);
            log.warn("Payment failed for Order ID: {}. Gateway Txn: {}", request.orderId(), saved.getPaymentGatewayTxnId());
            throw new BusinessRuleException("Payment failed via gateway: transaction declined");
        }

        //TODO: actual logic - payment gateway call

        //simulation successfull payment processing

        transaction.setStatus(PaymentStatus.PAID);
        transaction.setPaymentGatewayTxnId("GATEWAY-PAID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setPaymentGatewaySignature("GATEWAY-SIGNATURE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setPaymentGatewayOrderId("GATEWAY-ORDERID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Transaction saved = transactionRepository.save(transaction);
        log.info("Payment processed successfully for Order ID: {}. Txn ID: {}, Gateway Txn: {}",
                request.orderId(), saved.getTransactionId(), saved.getPaymentGatewayTxnId());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paymentresponse> getPaymentByOrderId(Long orderId) {
        log.info("Fetching payment for order Id: {}",orderId);
        return transactionRepository.findByOrderId(orderId)
                .stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Paymentresponse getPaymentByTransactionId(String transactionId) {
        log.info("Fetching payment for transaction Id: {}",transactionId);
         Transaction transaction=transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(()->
                        new ResourceNotfoundException("Transaction Not found for ID: "+transactionId));
         return toResponse(transaction);
    }

    private Paymentresponse toResponse(Transaction txn){
        return new Paymentresponse(
                txn.getId(),
                txn.getTransactionId(),
                txn.getOrderId(),
                txn.getAmount(),
                txn.getPaymentMethod(),
                txn.getStatus(),
                txn.getPaymentGatewayTxnId(),
                txn.getCreatedAt(),
                txn.getUpdatedAt()
        );
    }
}
