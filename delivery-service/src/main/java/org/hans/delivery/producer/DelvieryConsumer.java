package org.hans.delivery.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hans.delivery.entity.DeliveryOrder;
import org.hans.delivery.entity.DeliveryOrderItem;
import org.hans.delivery.dto.OrderCreatedEvent;
import org.hans.delivery.dto.OrderValidatedEvent;
import org.hans.delivery.repository.DeliveryOrderRepository;
import org.hans.delivery.repository.InventoryRepository;
import org.hans.delivery.service.DeliveryService;
import org.hans.delivery.service.DecisionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@ApplicationScoped
public class DelvieryConsumer {

    private static final Logger log = LoggerFactory.getLogger(DelvieryConsumer.class);

    @Inject ObjectMapper objectMapper;

    @Inject
    DeliveryOrderRepository orderRepository;

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    DeliveryService deliveryService;

    @Inject
    OrderValidatedProducer validatedProducer;

    /**
     * Consume messages from 'order-create-in' channel (OrderCreatedEvent JSON).
     * Creates or updates the order in the delivery service database.
     * Implements basic idempotency: only processes orders that are PENDING.
     */
    @Incoming("order-create-in")
    @Transactional
    public void consume(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("DelvieryConsumer -> received event for orderId=" + event.id);

            if (event.id == null) {
                log.error("DelvieryConsumer -> event id is null, skipping");
                return;
            }

            DeliveryOrder order = orderRepository.findById(event.id);
            
            // Create order if it doesn't exist
            // Note: Since both services may use the same database, we try to find by the event ID
            // If using separate databases, we would need a different approach (e.g., external order ID field)
            if (order == null) {
                log.info("DelvieryConsumer -> creating new order from event id=" + event.id);
                order = new DeliveryOrder();
                // Note: With PanacheEntity, we cannot set ID manually
                // If both services share the same DB, the order should already exist
                // If using separate DBs, we would need to add an externalOrderId field
                order.status = "PENDING";
                order.createdAt = event.createdAt != null ? event.createdAt : Instant.now();
                order.clientName = event.customer;
                
                // Create order items from event
                if (event.items != null) {
                    for (OrderCreatedEvent.Item eventItem : event.items) {
                        DeliveryOrderItem item = new DeliveryOrderItem();
                        item.name = eventItem.name;
                        item.quantity = eventItem.quantity != null ? eventItem.quantity : 1;
                        order.addItem(item);
                    }
                }
                
                orderRepository.persist(order);
                log.info("DelvieryConsumer -> order created id=" + order.id);
            }

            // Idempotency: if not PENDING skip
            if (!"PENDING".equals(order.status)) {
                log.warn("DelvieryConsumer -> skipping order " + order.id + " status=" + order.status);
                return;
            }

            // Validate using domain service
            DecisionResult result = deliveryService.validate(event);
            log.info("DelvieryConsumer -> sending validated event for orderId=" + order.id);
            String newStatus = result.accepted() ? "ACCEPTED" : "DENIED";
            order.status = newStatus;
            orderRepository.persist(order);

            // Send validated event
            OrderValidatedEvent validated = new OrderValidatedEvent(order.id, newStatus, result.reason(), event.correlationId);
            validatedProducer.send(validated);

            System.out.println("OrderConsumer -> order " + order.id + " updated to " + newStatus + " reason=" + result.reason());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}