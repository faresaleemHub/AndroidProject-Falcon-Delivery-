package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class Merchant implements Serializable {
    private String id,email,password,username,companyName,phoneNumber,dateRegister,ProfileImgURI;

    public Merchant() {
    }

    public Merchant(String id, String username, String companyName, String phoneNumber, String dateRegister) {
        this.id = id;
        this.username = username;
        this.companyName = companyName;
        this.phoneNumber = phoneNumber;
        this.dateRegister = dateRegister;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return username;
    }

    public void setName(String name) {
        this.username = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
