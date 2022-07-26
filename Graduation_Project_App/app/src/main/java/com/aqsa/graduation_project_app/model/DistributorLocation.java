package com.aqsa.graduation_project_app.model;

public class DistributorLocation {
    private String id;
    private Double latPoint,longPoint;
    private String date;

    public DistributorLocation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLongPoint() {
        return longPoint;
    }

    public void setLongPoint(Double longPoint) {
        this.longPoint = longPoint;
    }

    public Double getLatPoint() {
        return latPoint;
    }

    public void setLatPoint(Double latPoint) {
        this.latPoint = latPoint;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
