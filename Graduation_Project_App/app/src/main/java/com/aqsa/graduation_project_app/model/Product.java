package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class Product implements Serializable {

    private String id,name,description,ImgURL,categoryID,dateRegister,price,parCode;

    public Product(){}

    public Product(String id, String name, String description, String img, String categoryID, String dateRegister, String price,String parCode) {
        this.id = id;
        this.name = name;
        this.description = description;
        ImgURL = img;
        this.categoryID = categoryID;
        this.dateRegister = dateRegister;
        this.price = price;
        this.parCode=parCode;
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

    public String getImg() {
        return ImgURL;
    }

    public void setImg(String img) {
        ImgURL = img;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateRegister() {
        return dateRegister;
    }

    public void setDateRegister(String dateRegister) {
        this.dateRegister = dateRegister;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImgURL() {
        return ImgURL;
    }

    public void setImgURL(String imgURL) {
        ImgURL = imgURL;
    }

    public String getParCode() {
        return parCode;
    }

    public void setParCode(String parCode) {
        this.parCode = parCode;
    }
}
