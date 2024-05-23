package com.webapp.cleanease_laundry_system.order;

import com.webapp.cleanease_laundry_system.shipping.details.ShippingDetails;
import com.webapp.cleanease_laundry_system.user.User;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Date orderDate;
    private String orderStatus;
    private Double orderAmount;
    private int numBaskets;

    @ManyToOne
    @JoinColumn(name = "shipping_id")
    private ShippingDetails shippingDetails;

    public Order(User user, Date orderDate, String orderStatus, Double orderAmount, int numBaskets, ShippingDetails shippingDetails) {
        this.user = user;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.orderAmount = orderAmount;
        this.numBaskets = numBaskets;
        this.shippingDetails = shippingDetails;
    }

    public Order() {
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public int getNumBaskets() {
        return numBaskets;
    }

    public void setNumBaskets(int numBaskets) {
        this.numBaskets = numBaskets;
    }

    public ShippingDetails getShippingDetails() {
        return shippingDetails;
    }

    public void setShippingDetails(ShippingDetails shippingDetails) {
        this.shippingDetails = shippingDetails;
    }
}

