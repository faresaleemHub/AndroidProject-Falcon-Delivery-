package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class Vehicle implements Serializable {
    private String id,name, VNumber,DistributorID,DateRegister;

    public Vehicle() {
    }

    public Vehicle(String id,String name, String VNumber, String distributorID, String DateRegister) {
        this.name = name;
        this.VNumber = VNumber;
        DistributorID = distributorID;
        this.DateRegister=DateRegister;
        this.id=id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateRegister() {
        return DateRegister;
    }

    public void setDateRegister(String dateRegister) {
        DateRegister = dateRegister;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVNumber() {
        return VNumber;
    }

    public void setVNumber(String VNumber) {
        this.VNumber = VNumber;
    }

    public String getDistributorID() {
        return DistributorID;
    }

    public void setDistributorID(String distributorID) {
        DistributorID = distributorID;
    }
}
