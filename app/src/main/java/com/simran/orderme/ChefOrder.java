package com.simran.orderme;

import java.util.List;

public class ChefOrder {
    private String orderId;
    private List<Food> foodList;
    private String userId; // ID of the user (Foodie) who placed the order

    public ChefOrder() {
        // Default constructor required for calls to DataSnapshot.getValue(ChefOrder.class)
    }

    public ChefOrder(String orderId, List<Food> foodList, String userId) {
        this.orderId = orderId;
        this.foodList = foodList;
        this.userId = userId;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<Food> getFoodList() {
        return foodList;
    }

    public void setFoodList(List<Food> foodList) {
        this.foodList = foodList;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
