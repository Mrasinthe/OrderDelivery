package com.esi.deliveryservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.esi.deliveryservice.dto.OrderDto;
import com.esi.deliveryservice.model.Order;
import com.esi.deliveryservice.repository.DeliveryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @KafkaListener(topics = "orderReadyTopic", groupId = "orderReadyGroup")
    public void processOrderReady(OrderDto orderDto) {
        Order order = mapToOrder(orderDto);
        Order saved = deliveryRepository.save(order);
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

    public List<OrderDto> getAllOrdersDeliveries() {
        List<OrderDto> orderDtos = new ArrayList<>();
        deliveryRepository.findAll().forEach(order -> {
            OrderDto orderDto = mapToOrderDto(order);
            orderDtos.add(orderDto);
        });
        log.info("orderDtos: {}", orderDtos);
        return orderDtos;
    }

    private OrderDto mapToOrderDto(Order order) {
        String orderUrl = "http://localhost:8083/api/orders/" + order.getId();
        WebClient webClientBuilder = WebClient.create(); // Create a new WebClient

        // Use WebClient to make a GET request and retrieve the OrderDto
        OrderDto orderDto = webClientBuilder.get()
                .uri(orderUrl)
                .retrieve()
                .bodyToMono(OrderDto.class)
                .block();
        log.info("orderDto: {}", orderDto);

        // Build a new OrderDto using the retrieved data
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .address(orderDto.getAddress())
                .pizzaCode(order.getPizzaCode())
                .pizzaQuantity(order.getPizzaQuantity())
                .orderStatus(order.getOrderStatus())
                .build();
    }

}
