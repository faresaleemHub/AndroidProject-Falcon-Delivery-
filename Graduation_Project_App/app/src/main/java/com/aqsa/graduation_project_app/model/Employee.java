package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class Employee implements Serializable {
    private String id, username, email, password, phone, dateRegister, jobType, salary,
            country, vehicle_id,//for Distributor
            ProfileImgURI;

    public Employee() {
    }

    public Employee(String id, String username, String email, String password, String phone,
                    String dateRegister, String jobType, String salary, String country) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.dateRegister = dateRegister;
        this.jobType = jobType;
        this.salary = salary;
        this.country = country;
        this.email = email;
    }

    public String getvehicle_id() {
        return vehicle_id;
    }

    public void setvehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
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

    public String getDateRegister() {
        return dateRegister;
    }

    public void setDateRegister(String dateRegister) {
        this.dateRegister = dateRegister;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImgURI() {
        return ProfileImgURI;
    }

    public void setProfileImgURI(String profileImgURI) {
        ProfileImgURI = profileImgURI;
    }
}
