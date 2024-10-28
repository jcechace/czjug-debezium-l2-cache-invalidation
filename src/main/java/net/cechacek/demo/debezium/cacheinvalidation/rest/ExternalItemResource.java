package net.cechacek.demo.debezium.cacheinvalidation.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import net.cechacek.demo.debezium.cacheinvalidation.model.Item;
import net.cechacek.demo.debezium.cacheinvalidation.persistence.config.DataSourceConfig;
import net.cechacek.demo.debezium.cacheinvalidation.rest.dto.UpdateItemRequest;
import net.cechacek.demo.debezium.cacheinvalidation.rest.dto.UpdateItemResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * REST resource for external item management.
 * Same as {@link ItemResource}, but hibernate is circumvented .
 * Thus, changes are not reflected in the cache.
 */
@Path("/external/items")
public class ExternalItemResource {
    public static final String SQL_SELECT = "SELECT * FROM public.item WHERE id = ?;";
    public static final String SQL_UPDATE = "UPDATE public.item SET description = ?, price = ? WHERE id = ?;";

    @Inject
    DataSourceConfig dsConfig;

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") long id) {
        try(var conn = getDbConnection(); var statement = conn.prepareStatement(SQL_SELECT)) {
            statement.setLong(1, id);
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                // construct response
                var item = new Item();
                item.setId(rs.getLong("id"));
                item.setDescription(rs.getString("description"));
                item.setPrice(rs.getBigDecimal("price"));

                return Response.ok(item).build();
            }
        } catch (SQLException e) {
            return Response.status(500).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") long id, UpdateItemRequest request) {
        var item = Item.<Item>findById(id);

        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        //construct response
        var response = new UpdateItemResponse();
        response.setId(id);
        response.setOldDescription(item.getDescription());
        response.setOldPrice(item.getPrice());
        response.setNewDescription(request.getDescription());
        response.setNewPrice(request.getPrice());

        // update the item directly (simulating external system)
        try(var conn = getDbConnection(); var statement = conn.prepareStatement(SQL_UPDATE)) {
            statement.setString(1, request.getDescription());
            statement.setBigDecimal(2, request.getPrice());
            statement.setLong(3, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            return Response.status(500).build();
        }


        return Response.ok(response).build();
    }

    private Connection getDbConnection() throws SQLException {
        return DriverManager.getConnection(dsConfig.url(), dsConfig.username(), dsConfig.password());
    }
}
