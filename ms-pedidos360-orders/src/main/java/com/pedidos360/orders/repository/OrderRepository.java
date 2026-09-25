package com.pedidos360.orders.repository;

import com.pedidos360.orders.model.Order;
import com.pedidos360.orders.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserEmail(String userEmail);

    List<Order> findByStatusIn(Collection<OrderStatus> statuses);
}