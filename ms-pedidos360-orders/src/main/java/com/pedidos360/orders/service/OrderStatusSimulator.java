package com.pedidos360.orders.service;

import com.pedidos360.orders.model.Order;
import com.pedidos360.orders.model.OrderStatus;
import com.pedidos360.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class OrderStatusSimulator {

    private final OrderRepository orderRepository;
    private final Random random = new SecureRandom();

    @Value("${orders.simulation.enabled:true}")
    private boolean enabled;

    @Value("${orders.simulation.cancel-rate:0.3}")
    private double cancelRate;

    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.RECIBIDO,
            OrderStatus.CONFIRMADO,
            OrderStatus.PREPARANDO,
            OrderStatus.LISTO);

    @Scheduled(fixedDelayString = "${orders.simulation.interval-ms:6000}")
    @Transactional
    public void advancePending() {
        if (!enabled) {
            return;
        }
        for (Order order : orderRepository.findByStatusIn(ACTIVE_STATUSES)) {
            order.setStatus(resolveNext(order.getStatus()));
        }
    }

    private OrderStatus resolveNext(OrderStatus current) {
        if ((current == OrderStatus.RECIBIDO || current == OrderStatus.CONFIRMADO)
                && random.nextDouble() < cancelRate) {
            return OrderStatus.CANCELADO;
        }
        return switch (current) {
            case RECIBIDO -> OrderStatus.CONFIRMADO;
            case CONFIRMADO -> OrderStatus.PREPARANDO;
            case PREPARANDO -> OrderStatus.LISTO;
            case LISTO -> OrderStatus.ENTREGADO;
            default -> current;
        };
    }
}