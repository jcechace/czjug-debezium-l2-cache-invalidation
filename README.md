# JPA Cache Invalidation

This demo shows how Debezium can be used to invalidate items in the JPA 2nd level cache after external data changes,
e.g. a manual record update in the database, bypassing the application layer.

The application uses Quarkus, Hibernate, and PostgreSQL as a database.
The domain model is centered around purchase orders of given items.
The `Item` entity is marked as cacheable, i.e. after updates to an item (e.g. its base price),
it must be purged from the 2nd-level cache in order to correctly calculate the price of future orders of that item.

## Manual Testing
1. Run the application using the Quarkus development mode.
   ```bash
   mvn clean quarkus:dev
   ``` 
   The application will start in the development mode and run until you press `Ctrl+C` to stop the application.

2. Place an order for item 1003 using curl:
   ```bash
   curl -H "Content-Type: application/json" \
      -X POST \
      --data @resources/data/create-order-request.json \
      http://localhost:8080/rest/orders   
   ```
   Or, if [httpie](https://httpie.org/) is your preferred CLI HTTP client:
   ```bash
   cat resources/data/create-order-request.json | http POST http://localhost:8080/rest/orders
   ```

3. Update the item 10003 directly in the database (using HTTP endpoint simulating external access):
      ```bash
   curl -H "Content-Type: application/json" \
      -X PUT \
      --data @resources/data/update-item-request-ext.json \
      http://localhost:8080/rest/external/items/10003
   ```
   or via httpie:
   ```bash
   cat resources/data/update-item-request-ext.json | http PUT http://localhost:8080/rest/external/items/10003
   ```

5. Now use the REST endpoint to verify that the item has been purged from the cache:
   ```bash
   curl -H "Content-Type: application/json" \
      -X GET \
      http://localhost:8080/rest/cache/item/10003
   ```
   or via httpie:
   ```bash
   http GET http://localhost:8080/rest/cache/item/10003
   ```

6. Place another order of that item and observe how the calculated total price reflects the change applied above.
   Also observe the application's log how the `item` table is queried.

7. Now, update the item again using the application's REST endpoint this time:
   ```bash
   curl -H "Content-Type: application/json" \
      -X PUT \
      --data @resources/data/update-item-request.json \
      http://localhost:8080/rest/items/10003
   ```
   or via httpie:
   ```bash
   cat resources/data/update-item-request.json | http PUT http://localhost:8080/rest/items/10003
   ```

8. The Debezium CDC event handler detects this transaction is issued by the application, which results in the item not being removed from the cache:
   You can test this use case using curl:
   ```bash
   curl -H "Content-Type: application/json" \
      -X GET \
      http://localhost:8080/rest/cache/item/10003
   ```
   or using httpie:
   ```bash
   http GET http://localhost:8080/rest/cache/item/10003
   ```

9. If you place another order, the `Item` entity is obtained from the cache, avoiding the database round-trip.

10. Press `Ctrl+C` in the terminal to stop the Quarkus running application.


### Shutdown steps

1. Stop the Quarkus application by pressing `Ctrl+C` in the terminal.