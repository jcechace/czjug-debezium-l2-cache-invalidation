package net.cechacek.demo.debezium.cacheinvalidation.rest.dto;

import net.cechacek.demo.debezium.cacheinvalidation.model.Item;

import java.math.BigDecimal;

public class CreateOrderResponse {
    private long id;
    private String customer;
    private Item item;
    private int quantity;
    private BigDecimal totalPrice;

    public CreateOrderResponse(long id, String customer, Item item, int quantity, BigDecimal totalPrice) {
        this.id = id;
        this.customer = customer;
        this.item = item;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
