package org.hans.orders.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long productId;
    public String name;
    public int quantity;
    public String additionalInformation;

    @ManyToOne(fetch = FetchType.LAZY)
    public OrderEntity order;
}