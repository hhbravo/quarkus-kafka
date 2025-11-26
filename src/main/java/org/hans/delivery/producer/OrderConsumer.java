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

@ApplicationScoped
public class OrderConsumer {


    @Inject ObjectMapper MAPPER;

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
            OrderCreatedEvent event = MAPPER.readValue(payload, OrderCreatedEvent.class);
            System.out.println("OrderConsumer -> received event for orderId=" + event.id);

            if (event.id == null) {
                System.err.println("OrderConsumer -> missing order id");
                return;
            }

            DeliveryOrder order = orderRepository.findById(event.id);
            if (order == null) {
                System.err.println("OrderConsumer -> order not found: " + event.id);
                return;
            }

            // Idempotency: if not PENDING skip
            if (!"PENDING".equals(order.status)) {
                System.out.println("OrderConsumer -> skipping order " + order.id + " status=" + order.status);
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