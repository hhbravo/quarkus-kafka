package org.hans.orders.dto;

public record OrderItemEvent(
        Long productId,
        String name,
        Integer quantity,
        String additionalInformation
) {}