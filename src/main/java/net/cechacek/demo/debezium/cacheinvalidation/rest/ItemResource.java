package net.cechacek.demo.debezium.cacheinvalidation.rest;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import net.cechacek.demo.debezium.cacheinvalidation.model.Item;
import net.cechacek.demo.debezium.cacheinvalidation.rest.dto.UpdateItemRequest;
import net.cechacek.demo.debezium.cacheinvalidation.rest.dto.UpdateItemResponse;

@Path("/items")
public class ItemResource {

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") long id) {
        var item = Item.<Item>findById(id);

        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(item).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    public Response update(@PathParam("id") long id, UpdateItemRequest request) {
        var item = Item.<Item>findById(id);

        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var response = new UpdateItemResponse();
        response.setId(id);
        response.setOldDescription(item.getDescription());
        response.setOldPrice(item.getPrice());
        response.setNewDescription(request.getDescription());
        response.setNewPrice(request.getPrice());

        // update the item
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());


        return Response.ok(response).build();
    }
}
