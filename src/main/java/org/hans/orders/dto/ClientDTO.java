package org.hans.orders.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientDTO(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String phoneNumber,
        @NotBlank String documentNumber,
        @NotBlank String documentType
) {
}
