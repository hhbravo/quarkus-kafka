package org.hans.orders.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.hans.orders.dto.*;
import org.hans.orders.entity.OrderEntity;
import org.hans.orders.entity.OrderItemEntity;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderEntity toEntity(OrderRequest request) {
        OrderEntity order = new OrderEntity();
        ClientDTO c = request.client();
        order.clientName = c.name();
        order.clientAddress = c.address();
        order.clientPhone = c.phoneNumber();
        order.clientDocumentNumber = c.documentNumber();
        order.clientDocumentType = c.documentType();
        request.items().stream().map(this::toItemEntity).forEach(order::addItem);
        return order;
    }

    public OrderItemEntity toItemEntity(OrderItemDTO dto) {
        OrderItemEntity item = new OrderItemEntity();
        item.productId = dto.productId();
        item.name = dto.name();
        item.quantity = dto.quantity();
        item.additionalInformation = dto.additionalInformation();
        return item;
    }

//    public String toEventPayload(OrderEntity order) {
//        try {
//            return objectMapper.writeValueAsString(order);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to serialize order event", e);
//        }
//    }


    public String toEventPayload(OrderEntity order) {
        try {
            List<OrderItemEvent> items = (order.items == null)
                    ? List.of()
                    : order.items.stream()
                    .map(i -> new OrderItemEvent(i.productId, i.name, i.quantity, i.additionalInformation))
                    .collect(Collectors.toList());

            OrderEvent event = new OrderEvent(
                    order.clientName,
                    order.clientAddress,
                    order.clientPhone,
                    order.clientDocumentNumber,
                    order.clientDocumentType,
                    order.status,
                    items
            );

            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize order event", e);
        }
    }
}