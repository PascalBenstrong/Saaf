package com.bitec.saafs.models;

public class Friends extends UserId {

    private String name,username, image, email, token_id;

    public Friends() {

    }

    public Friends(String name, String username, String image, String email, String token_id) {
        this.name = name;
        this.username = username;
        this.image = image;
        this.email = email;
        this.token_id = token_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }
}
