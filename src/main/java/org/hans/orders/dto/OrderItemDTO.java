package org.hans.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderItemDTO(
        Long productId,
        @NotBlank String name,
        @Min(1) int quantity,
        String additionalInformation
) {
}
