package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class Client implements Serializable {
    private String id, username, email, password, phone, marketName, dateRegister;

    private String ProfileImgURI;

    public Client() {
    }

    public Client(String id, String username, String email, String password, String phone, String marketName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.marketName = marketName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getDateRegister() {
        return dateRegister;
    }

    public void setDateRegister(String dateRegister) {
        this.dateRegister = dateRegister;
    }

    public String getProfileImgURI() {
        return ProfileImgURI;
    }

    public void setProfileImgURI(String profileImgURI) {
        ProfileImgURI = profileImgURI;
    }

}
