package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class Category implements Serializable {
    private String name,ImgURL,id,dateRegister;

    public Category() {
    }

    public Category(String name, String img, String id, String dateRegister) {
        this.name = name;
        ImgURL = img;
        this.id = id;
        this.dateRegister = dateRegister;
    }

    public String getDateRegister() {
        return dateRegister;
    }

    public void setDateRegister(String dateRegister) {
        this.dateRegister = dateRegister;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return ImgURL;
    }

    public void setImg(String img) {
        ImgURL = img;
    }
}
