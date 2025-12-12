package org.hans.orders.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hans.orders.dto.OrderValidatedEvent;
import org.hans.orders.entity.Order;
import org.hans.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OrderValidatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderValidatedConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    OrderRepository orderRepository;

    /**
     * Consume messages from 'orderValidated' topic.
     * Updates the order status based on the validation result from stock-service.
     */
    @Incoming("order-validated-in")
    @Transactional
    public void consume(String payload) {
        try {
            OrderValidatedEvent event = objectMapper.readValue(payload, OrderValidatedEvent.class);
            log.info("OrderValidatedConsumer -> received validation event for orderId={}, status={}", 
                    event.orderId, event.status);

            if (event.orderId == null) {
                log.error("OrderValidatedConsumer -> event orderId is null, skipping");
                return;
            }

            Order order = orderRepository.findById(event.orderId);
            if (order == null) {
                log.error("OrderValidatedConsumer -> order not found id={}", event.orderId);
                return;
            }

            // Update order status based on validation result
            // Status can be "ACCEPTED" or "DENIED"
            String newStatus = event.status != null ? event.status : "DENIED";
            order.status = newStatus;
            orderRepository.persist(order);

            log.info("OrderValidatedConsumer -> order {} updated to status={}, reason={}", 
                    order.id, newStatus, event.reason);
        } catch (Exception e) {
            log.error("OrderValidatedConsumer -> error processing validation event", e);
        }
    }
}

