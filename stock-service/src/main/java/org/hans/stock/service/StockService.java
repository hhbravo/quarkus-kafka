package org.hans.stock.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hans.stock.entity.InventoryItem;
import org.hans.stock.repository.InventoryRepository;
import org.hans.stock.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@ApplicationScoped
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    @Inject
    InventoryRepository inventoryRepository;

    /**
     * Validates against inventory. Reserves are NOT made here (this is a simple check).
     * Returns DecisionResult with reason.
     */
    public DecisionResult validate(OrderCreatedEvent event) {
        if (event.items == null || event.items.isEmpty()) {
            return new DecisionResult("DENIED", "items is null or empty");
        }

        // Aggregate quantities by item name
        Map<String, Integer> needed = new HashMap<>();
        for (OrderCreatedEvent.Item it : event.items) {
            int q = it.quantity != null ? it.quantity : 1;
            needed.put(it.name, needed.getOrDefault(it.name, 0) + q);
        }

        // Check inventory
        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            String name = e.getKey();
            Integer required = e.getValue();
            InventoryItem inv = inventoryRepository.findByName(name);
            if (inv == null) {
                log.error("DeliveryService -> Item not found: " + name);
                return new DecisionResult("DENIED", "Item not found: " + name);
            }
            if (inv.getStock() == null || inv.getStock() < required) {
                log.error("DeliveryService -> Insufficient stock for: " + name);
                return new DecisionResult("DENIED", "Insufficient stock for: " + name);
            }
        }
        log.info("DeliveryService -> All items available");
        return new DecisionResult("ACCEPTED", "All items available");
    }
}
