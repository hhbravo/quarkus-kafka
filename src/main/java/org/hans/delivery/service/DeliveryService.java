package org.hans.delivery.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hans.delivery.entity.InventoryItem;
import org.hans.delivery.repository.InventoryRepository;
import org.hans.delivery.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@ApplicationScoped
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
    @Inject
    InventoryRepository inventoryRepository;

    /**
     * Validates against inventory. Reserves are NOT made here (this is a simple check).
     * Returns DecisionResult with reason.
     */
    public DecisionResult validate(OrderCreatedEvent event) {
        if (event == null || event.items == null || event.items.isEmpty()) {
            return new DecisionResult(false, "No items in order");
        }

        // Aggregate quantities by item name
        Map<String, Integer> needed = new HashMap<>();
        for (OrderCreatedEvent.Item it : event.items) {
            log.info("Validating item: " + it.name + " qty: " + it.quantity);
            int q = it.quantity != null ? it.quantity : 1;
            needed.put(it.name, needed.getOrDefault(it.name, 0) + q);
        }

        // Check inventory
        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            String name = e.getKey();
            Integer required = e.getValue();
            log.info("Checking inventory for item: " + name + " required qty: " + required);
            InventoryItem inv = inventoryRepository.findByName(name);
            log.info("Inventory check for item: " + name + " required: " + required +
                    " found stock: " + (inv != null ? inv.getStock() : "null"));
            if (inv == null) {
                return new DecisionResult(false, "Item not found: " + name);
            }
            if (inv.getStock() == null || inv.getStock() < required) {
                return new DecisionResult(false, "Insufficient stock for: " + name);
            }
        }

        return new DecisionResult(true, "All items available");
    }
}
