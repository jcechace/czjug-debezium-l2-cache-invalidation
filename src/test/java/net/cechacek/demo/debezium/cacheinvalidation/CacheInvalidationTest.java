/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.cechacek.demo.debezium.cacheinvalidation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class CacheInvalidationTest {

    @BeforeEach
    public void prepareItem() {
        updateItemInternal(10003, "North by Northwest", 14.99F);
    }

    @Test
    public void shouldInvalidateCacheAfterDatabaseUpdate() throws Exception {
        placeOrder(10003, 2, 29.98F);

        // update the item price directly in the DB
        updateItemExternal(10003, "North by Northwest (External)",16.99F);

        // cache should be invalidated
        await()
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> !get("/rest/cache/item/10003").as(boolean.class));

        // and the item reloaded from the DB
        placeOrder(10003, 2, 33.98F);
    }

    @Test
    public void shouldNotInvalidateCacheAfterUpdateThroughApplication() throws Exception {
        placeOrder(10003, 2, 29.98F);

        // update the item price through application
        updateItemInternal(10003, "North by Northwest", 16.99F);

        // Theoretically an (unexpected) CDC event could also arrive after that time,
        // but that seems to be as good as it gets
        await()
                .pollDelay(3, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> get("/rest/cache/item/10003").as(boolean.class));
    }

    private void placeOrder(long itemId, int quantity, float expectedTotalPrice) {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "customer" : "Billy-Bob",
                        "itemId" : %d,
                        "quantity" : %d
                    }
                    """.formatted(itemId, quantity)
            )
        .when()
            .post("/rest/orders")
        .then()
            .body("totalPrice", equalTo(expectedTotalPrice));
    }

    private void updateItemInternal(long itemId, String newDescription, float newPrice) {
        updateItem("/rest/items/{id}", itemId, newDescription, newPrice);
    }

    private void updateItemExternal(long itemId, String newDescription, float newPrice) {
        updateItem("/rest/external/items/{id}", itemId, newDescription, newPrice);
    }

    private void updateItem(String endpoint, long itemId, String newDescription, float newPrice) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "description" : "%s",
                        "price" : %f
                    }
                    """.formatted(newDescription, newPrice)
                )
                .when()
                .put(endpoint, itemId)
                .then()
                .statusCode(200);
    }
}
