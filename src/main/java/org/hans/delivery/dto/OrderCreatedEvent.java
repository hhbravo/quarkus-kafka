package org.hans.delivery.dto;

import java.time.Instant;
import java.util.List;

public class OrderCreatedEvent {
    public Long id;
    public String customer;
    public List<Item> items;
    public Double total;
    public String correlationId;
    public Integer eventVersion;
    public Instant createdAt;

    public static class Item {
        public String name;
        public Integer quantity;

        public Item() {}
        public Item(String name, Integer quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }

    public OrderCreatedEvent() {}
}