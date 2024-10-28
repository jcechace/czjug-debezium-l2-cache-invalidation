package net.cechacek.demo.debezium.cacheinvalidation.rest;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import net.cechacek.demo.debezium.cacheinvalidation.model.Item;
import net.cechacek.demo.debezium.cacheinvalidation.model.PurchaseOrder;
import net.cechacek.demo.debezium.cacheinvalidation.rest.dto.CreateOrderRequest;
import net.cechacek.demo.debezium.cacheinvalidation.rest.dto.CreateOrderResponse;

import java.math.BigDecimal;

@Path("/orders")
public class OrderResource {

    @POST
    @Transactional
    public Response create(CreateOrderRequest orderRequest) {
        var item = Item.<Item>findById(orderRequest.getItemId());

        PurchaseOrder po = new PurchaseOrder(
                orderRequest.getCustomer(),
                item,
                orderRequest.getQuantity(),
                item.getPrice().multiply(BigDecimal.valueOf(orderRequest.getQuantity()))
        );

        po.persist();

        var response = new CreateOrderResponse(
                po.getId(),
                po.getCustomer(),
                po.getItem(),
                po.getQuantity(),
                po.getTotalPrice()
        );

        return Response.ok(response).build();
    }
}
