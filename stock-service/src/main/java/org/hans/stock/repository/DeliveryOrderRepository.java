package org.hans.stock.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.hans.stock.entity.DeliveryOrder;

@ApplicationScoped
public class DeliveryOrderRepository implements PanacheRepository<DeliveryOrder> {

}
