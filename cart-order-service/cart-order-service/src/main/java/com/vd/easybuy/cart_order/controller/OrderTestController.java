package com.vd.easybuy.cart_order.controller;

import com.vd.easybuy.cart_order.dto.OrderCreateRequest;
import com.vd.easybuy.cart_order.dto.ProductResponse;
import com.vd.easybuy.cart_order.service.OrderTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Slf4j
@RequiredArgsConstructor
public class OrderTestController {

    private final OrderTestService orderTestService;

    @PostMapping("/orders")
    public ResponseEntity<ProductResponse> createOrder(@RequestBody OrderCreateRequest request){
        log.info("OrderCreateRequest received{}",request);
        return ResponseEntity.ok(orderTestService.createOrder(request));
    }

    @PostMapping("/ordersWIthRest")
    public ResponseEntity<ProductResponse> createOrderwithRest(@RequestBody OrderCreateRequest request){
        log.info("OrderCreateRequest received{}",request);
        return ResponseEntity.ok(orderTestService.createOrderWithRestClient(request));
    }

    @PostMapping("/ordersWIthFeign")
    public ResponseEntity<ProductResponse> createOrderwithFeign(@RequestBody OrderCreateRequest request){
        log.info("OrderCreateRequest received{}",request);
        return ResponseEntity.ok(orderTestService.createOrderWithFeign(request));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}