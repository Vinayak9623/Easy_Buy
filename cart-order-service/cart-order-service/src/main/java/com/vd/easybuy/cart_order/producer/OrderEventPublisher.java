package com.vd.easybuy.cart_order.producer;

import com.vd.easybuy.common.events.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class OrderEventPublisher {

    private final Logger logger= LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String,Object> kafkaTemplate;

    private final String ORDER_TOPIC="order-topic";

    public OrderEventPublisher(KafkaTemplate<String,Object> kafkaTemplate){
        this.kafkaTemplate=kafkaTemplate;
    }

    public void publishOrderCreatedEvent(OrderEvent orderEvent){
        try{
            this.kafkaTemplate.send(ORDER_TOPIC,orderEvent);
            logger.info("order event publish successfully");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
