package net.cechacek.demo.debezium.cacheinvalidation.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import net.cechacek.demo.debezium.cacheinvalidation.model.Item;

@Path("/cache")
public class CacheResource {
    @Inject
    @PersistenceContext
    EntityManagerFactory entityManagerFactory;

    @DELETE
    @Path("/item/{id}")
    public void invalidateItemCacheEntry(@PathParam("id") long itemId) {
        entityManagerFactory.getCache().evict(Item.class, itemId);
    }

    @GET
    @Produces("application/json")
    @Path("/item/{id}")
    public boolean isContained(@PathParam("id") long itemId) {
        return entityManagerFactory.getCache().contains(Item.class, itemId);
    }
}
