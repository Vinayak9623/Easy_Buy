package com.vd.easybuy.cart_order.service.impl;

import com.vd.easybuy.cart_order.client.InventoryClient;
import com.vd.easybuy.cart_order.client.ProductClientTest;
import com.vd.easybuy.cart_order.dto.OrderCreateRequest;
import com.vd.easybuy.cart_order.dto.ProductResponse;
import com.vd.easybuy.cart_order.repository.CartRepository;
import com.vd.easybuy.cart_order.repository.OrderRepository;
import com.vd.easybuy.cart_order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final RestTemplate restTemplate;
    private final RestClient restClient;
    private final ProductClientTest productClientTest;


    @Override
    public ProductResponse createOrder(OrderCreateRequest orderCreateRequest) {

        String productId = orderCreateRequest.items().getFirst().productId();
        var producturl = "http://localhost:8081/api/products/"+productId;
        log.info("Product url {}", producturl);
        //call to product service

        try {
            //ResponseEntity<ProductResponse> response = restTemplate.getForEntity(producturl, ProductResponse.class);
            ProductResponse productResponse = restTemplate.getForObject(producturl, ProductResponse.class);
            log.info("get Product response {}", productResponse);

//            if(response.getStatusCode().is2xxSuccessful()){
//                log.info("we recive successfull response from product entity");
//            }
           //return response.getBody();
            return productResponse;
        } catch (HttpClientErrorException ex) {
             ex.printStackTrace();
             throw new RuntimeException("Product not found"+ex.getStatusCode());

        }catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException("something is wrong");
        }
    }

    @Override
    public ProductResponse createOrderWithRestClient(OrderCreateRequest orderCreateRequest) {

        String productId=orderCreateRequest.items().getFirst().productId();
        var productUrl="http://localhost:8081/api/products/"+productId;
        try{
            ProductResponse response = restClient.get().uri(productUrl)
                    .header(HttpHeaders.ACCEPT,"application/json")
                    .retrieve()
                    .body(ProductResponse.class);
            return response;
        }
        catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException("Product not found with given id");
        }
    }

    @Override
    public ProductResponse createOrderWithFeign(OrderCreateRequest orderCreateRequest) {
        return productClientTest.getPeoductById(orderCreateRequest.items().getFirst().productId());
    }

}
