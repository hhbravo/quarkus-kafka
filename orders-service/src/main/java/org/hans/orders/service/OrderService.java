package org.hans.orders.service;

import org.hans.orders.dto.OrderRequest;
import org.hans.orders.dto.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
}