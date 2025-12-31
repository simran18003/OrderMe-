package com.simran.orderme;

public class User {
    private String uid;
    private String email;
    private String name;
    private String role;
    private String profileImageUrl;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String uid, String email, String name, String role, String profileImageUrl) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public User(String name, String email, String role, String profileImage) {
    }

    // Getters and setters for each field
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
