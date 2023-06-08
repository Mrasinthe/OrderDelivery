package com.esi.kitchenservice.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.esi.kitchenservice.dto.OrderDto;
import com.esi.kitchenservice.dto.OrderStatus;
import com.esi.kitchenservice.model.Order;
import com.esi.kitchenservice.repository.KitchenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenService {

    @Autowired
    private KitchenRepository kitchenRepository;
    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    @KafkaListener(topics = "orderCreatedTopic", groupId = "orderCreateGroup")
    public void processOrderCreated(OrderDto orderDto) {
        Order order = mapToOrder(orderDto);
        Order saved = kitchenRepository.save(order);
        log.info("Saved Order object: {}", saved);
    }

    private Order mapToOrder(OrderDto orderDto) {
        return Order.builder()
                .id(orderDto.getId())
                .userId(orderDto.getUserId())
                .pizzaCode(orderDto.getPizzaCode())
                .pizzaQuantity(orderDto.getPizzaQuantity())
                .orderStatus(orderDto.getOrderStatus())
                .build();
    }

    public void orderReady(OrderDto orderDto) {

        Optional<Order> orderOpt = kitchenRepository.findById(orderDto.getId());
        if (orderOpt.isPresent()) {

            Order order = mapToOrder(orderDto);
            order.setOrderStatus(OrderStatus.Ready);
            orderDto.setOrderStatus(OrderStatus.Ready);
            Order saved = kitchenRepository.save(order);
            log.info("Updated Order object: {}", saved);

            kafkaTemplate.send("orderReadyTopic", orderDto);
        } else {
            log.error("Order not found");
        }

    }
}
