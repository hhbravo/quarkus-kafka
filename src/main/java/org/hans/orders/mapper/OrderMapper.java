package org.hans.orders.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.hans.orders.dto.*;
import org.hans.orders.entity.Order;
import org.hans.orders.entity.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(OrderMapper.class);

    public Order toEntity(OrderRequest request) {
        log.info("Mapping OrderRequest to Order entity for clientDocumentNumber={}", request.client().documentNumber());
        Order order = new Order();
        ClientDTO c = request.client();
        order.clientName = c.name();
        order.clientAddress = c.address();
        order.clientPhone = c.phoneNumber();
        order.clientDocumentNumber = c.documentNumber();
        order.clientDocumentType = c.documentType();
        order.total = request.total();
        order.status = "PENDING";

        request.items().stream().map(this::toItemEntity).forEach(order::addItem);
        return order;
    }

    public OrderItem toItemEntity(OrderItemDTO dto) {
        log.info("Mapping OrderItemDTO to OrderItem entity for productId={}", dto.productId());
        OrderItem item = new OrderItem();
        item.productId = dto.productId();
        item.name = dto.name();
        item.quantity = dto.quantity();
        item.additionalInformation = dto.additionalInformation();
        return item;
    }


//    public String toEventPayload(Order order) {
//        try {
//            List<OrderItemEvent> items = (order.items == null)
//                    ? List.of()
//                    : order.items.stream()
//                    .map(i -> new OrderItemEvent(i.productId, i.name, i.quantity, i.additionalInformation))
//                    .collect(Collectors.toList());
//
//            OrderEvent event = new OrderEvent(
//                    order.clientName,
//                    order.clientAddress,
//                    order.clientPhone,
//                    order.clientDocumentNumber,
//                    order.clientDocumentType,
//                    order.status,
//                    items
//            );
//
//            return objectMapper.writeValueAsString(event);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to serialize order event", e);
//        }
//    }

    public OrderCreatedEvent toCreatedEvent(Order order) {
        log.info("Mapping Order entity to OrderCreatedEvent for orderId={}", order.id);
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.id = order.id;
        event.customer = order.clientName;
        event.total = order.total;
        event.correlationId = UUID.randomUUID().toString();
        event.eventVersion = 1;
        event.createdAt = Instant.now();

        event.items = order.items.stream()
                .map(this::toEventItem)
                .collect(Collectors.toList());
        return event;
    }


    private OrderCreatedEvent.Item toEventItem(OrderItem item) {
        return new OrderCreatedEvent.Item(
                item.name,
                item.quantity != null ? item.quantity : 1
        );
    }
}