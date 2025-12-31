package com.simran.orderme;

public class Restaurant {
    private String name;
    private String description;
    private String imageUrl;
    private String restaurantName;

    // Default constructor for Firebase
    public Restaurant() {
    }

    public Restaurant(String name, String description, String imageUrl, String restaurantName) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.restaurantName = restaurantName;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
