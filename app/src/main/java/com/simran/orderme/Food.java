package com.simran.orderme;

public class Food {
    private String fid;
    private String name;
    private String description;
    private String price;
    private String image;
    private String userId;

    public Food() {
        // Required empty constructor for Firebase
    }

    public Food(String fid, String name, String description, String price, String image, String userId) {
        this.fid = fid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.userId = userId;
    }

    // Getters and setters for all fields
    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
