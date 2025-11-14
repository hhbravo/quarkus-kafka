package org.hans.orders.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hans.orders.dto.OrderRequest;
import org.hans.orders.dto.OrderResponse;
import org.hans.orders.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(OrderResource.class);


    @POST
    @Path("/create")
    public Response createOrder(@Valid OrderRequest request) {
        log.info("Received create order request for clientId={}", request.client().documentNumber());
        OrderResponse resp = orderService.createOrder(request);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }
}
