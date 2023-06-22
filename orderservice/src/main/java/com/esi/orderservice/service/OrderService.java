package com.esi.orderservice.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.esi.orderservice.dto.OrderDto;
import com.esi.orderservice.model.Order;
import com.esi.orderservice.model.OrderStatus;
import com.esi.orderservice.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    public void addOrder(OrderDto orderDto) {

        log.info("addOrder: {}", orderDto);
        Order order = Order.builder()
                .id(orderDto.getId())
                .userId(orderDto.getUserId())
                .pizzaCode(orderDto.getPizzaCode())
                .address(orderDto.getAddress())
                .pizzaQuantity(orderDto.getPizzaQuantity())
                .build();

        // Setting the order status to Received
        order.setOrderStatus(OrderStatus.Received);
        orderDto.setOrderStatus(OrderStatus.Received);
        // Save the order to in its current state in the Database
        orderRepository.save(order);

        // Push the orderDto to the orderCreatedTopic topic
        kafkaTemplate.send("orderCreatedTopic", orderDto);

    }

    public Optional<OrderDto> getOrder(String id) {
        Optional<Order> course = orderRepository.findById(id);
        return course.map(this::mapToOrderDto);
    }

    private OrderDto mapToOrderDto(Order course) {
        return OrderDto.builder()
                .id(course.getId())
                .userId(course.getUserId())
                .pizzaCode(course.getPizzaCode())
                .address(course.getAddress())
                .pizzaQuantity(course.getPizzaQuantity())
                .build();
    }

}
