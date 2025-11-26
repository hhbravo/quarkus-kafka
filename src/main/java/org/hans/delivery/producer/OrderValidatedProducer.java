package org.hans.delivery.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.hans.delivery.dto.OrderValidatedEvent;

@ApplicationScoped
public class OrderValidatedProducer {

    @Inject
    @Channel("events-validated-out")
    Emitter<String> emitter;

    @Inject ObjectMapper MAPPER;

    public void send(OrderValidatedEvent evt) {
        try {
            String payload = MAPPER.writeValueAsString(evt);
            emitter.send(payload);
            System.out.println("OrderValidatedProducer -> sent: " + payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}