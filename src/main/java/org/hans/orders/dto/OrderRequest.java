package org.hans.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotNull @Valid ClientDTO client,
        @NotEmpty @Valid List<OrderItemDTO> items,
        @NotNull @Valid Double total
) {}