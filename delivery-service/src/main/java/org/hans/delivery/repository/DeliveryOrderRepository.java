package org.hans.delivery.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.hans.delivery.entity.DeliveryOrder;


@ApplicationScoped
public class DeliveryOrderRepository implements PanacheRepository<DeliveryOrder> {

}
