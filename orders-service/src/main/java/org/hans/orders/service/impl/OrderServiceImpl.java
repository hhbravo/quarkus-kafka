package org.hans.orders.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hans.orders.dto.OrderRequest;
import org.hans.orders.dto.OrderResponse;
import org.hans.orders.entity.Order;
import org.hans.orders.mapper.OrderMapper;
import org.hans.orders.producer.OrderProducer;
import org.hans.orders.repository.OrderRepository;
import org.hans.orders.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Inject
    OrderRepository repository;

    @Inject
    OrderProducer producer;

    @Inject
    OrderMapper mapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        try {
            Order order = mapper.toEntity(request);
            repository.persist(order);
            log.info("Order persisted id={}", order.id);
            producer.send(order);
            return new OrderResponse(order.id, "Order created");
        } catch (Exception e) {
            log.error("Error creating order", e);
            throw e;
        }
    }
}