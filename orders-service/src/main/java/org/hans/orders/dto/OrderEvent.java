package org.hans.orders.dto;

import java.util.List;

public record OrderEvent(
        String clientName,
        String clientAddress,
        String clientPhone,
        String clientDocumentNumber,
        String clientDocumentType,
        String status,
        List<OrderItemEvent> items
) {}