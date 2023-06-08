package com.esi.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esi.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, String> {

}
