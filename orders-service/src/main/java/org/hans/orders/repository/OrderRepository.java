package org.hans.orders.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.hans.orders.entity.Order;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {
}
