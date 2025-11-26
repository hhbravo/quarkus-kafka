package org.hans.delivery.repository;


import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.hans.delivery.entity.InventoryItem;

@ApplicationScoped
public class InventoryRepository implements PanacheRepository<InventoryItem> {

    public InventoryItem findByName(String name) {
        return find("name", name).firstResult();
    }
}