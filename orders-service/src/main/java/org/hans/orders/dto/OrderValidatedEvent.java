package org.hans.orders.dto;

import java.time.Instant;

public class OrderValidatedEvent {
    public Long orderId;
    public String status; // ACCEPTED or DENIED
    public String reason;
    public String correlationId;
    public Instant validatedAt;

    public OrderValidatedEvent() {}
    
    public OrderValidatedEvent(Long orderId, String status, String reason, String correlationId) {
        this.orderId = orderId;
        this.status = status;
        this.reason = reason;
        this.correlationId = correlationId;
        this.validatedAt = Instant.now();
    }
}

