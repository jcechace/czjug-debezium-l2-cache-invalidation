package net.cechacek.demo.debezium.cacheinvalidation.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;

import java.math.BigDecimal;

@Entity
public class PurchaseOrder extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "sequence")
    @SequenceGenerator(
            name = "sequence",
            sequenceName = "seq_po",
            initialValue = 1001
    )
    private long id;

    private String customer;

    @ManyToOne
    private Item item;

    private int quantity;

    private BigDecimal totalPrice;

    public PurchaseOrder() {
    }

    public PurchaseOrder(String customer, Item item, int quantity, BigDecimal totalPrice) {
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
