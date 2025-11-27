package org.hans.delivery.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hans.delivery.entity.DeliveryOrder;
import org.hans.delivery.dto.OrderCreatedEvent;
import org.hans.delivery.dto.OrderValidatedEvent;
import org.hans.delivery.repository.DeliveryOrderRepository;
import org.hans.delivery.repository.InventoryRepository;
import org.hans.delivery.service.DeliveryService;
import org.hans.delivery.service.DecisionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Consume messages from 'events-in' channel (OrderCreatedEvent JSON).
     * Implements basic idempotency: only processes orders that are PENDING.
     */
    @Incoming("events-in")
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
            if (order == null) {
                log.error("DelvieryConsumer -> order not found id=" + event.id);
                return;
            }

            // Idempotency: if not PENDING skip
            if (!"PENDING".equals(order.status)) {
                log.error("DelvieryConsumer -> skipping order " + order.id + " status=" + order.status);
                return;
            }

            // Validate using domain service
            DecisionResult result = deliveryService.validate(event);
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