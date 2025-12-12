package org.hans.stock.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class DeliveryOrder extends PanacheEntity {

    public String status;
    public Instant createdAt;

    public String clientName;
    public String clientPhone;
    public String clientAddress;
    public String documentType;
    public String documentNumber;

    @OneToMany(mappedBy="deliveryOrder", cascade= CascadeType.ALL, orphanRemoval=true)
    public List<DeliveryOrderItem> items = new ArrayList<>();

    public void addItem(DeliveryOrderItem item) {
        items.add(item);
        item.deliveryOrder = this;
    }

}
