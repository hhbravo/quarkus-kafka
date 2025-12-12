package org.hans.stock.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class DeliveryOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long productId;
    public String name;
    public Integer quantity;
    public String additionalInformation;

    @ManyToOne(fetch = FetchType.LAZY)
    public DeliveryOrder deliveryOrder;
}