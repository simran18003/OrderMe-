package com.simran.orderme;

import com.simran.orderme.foodie.CartItem;

import java.util.List;

public class Order {
    private String orderId;
    private List<CartItem> foodItems; // Updated to use List<CartItem>
    private String address;
    private String paymentMethod;

    // Default constructor required for Firebase
    public Order() {
        // No-argument constructor
    }

    public Order(String orderId, List<CartItem> foodItems, String address, String paymentMethod) {
        this.orderId = orderId;
        this.foodItems = foodItems;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<CartItem> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<CartItem> foodItems) {
        this.foodItems = foodItems;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
