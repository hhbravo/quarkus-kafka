package org.hans.stock.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hans.stock.dto.OrderCreatedEvent;
import org.hans.stock.dto.OrderValidatedEvent;
import org.hans.stock.repository.DeliveryOrderRepository;
import org.hans.stock.repository.InventoryRepository;
import org.hans.stock.service.StockService;
import org.hans.stock.service.DecisionResult;
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
    StockService stockService;

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

    
            DecisionResult result = stockService.validate(event);

            // Send validated event
            OrderValidatedEvent validated = new OrderValidatedEvent(event.id, result.status(), result.reason(), event.correlationId);
            validatedProducer.send(validated);
            log.info("OrderConsumer -> order " + event.id + " updated to " + result.status() + " reason=" + result.reason());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}