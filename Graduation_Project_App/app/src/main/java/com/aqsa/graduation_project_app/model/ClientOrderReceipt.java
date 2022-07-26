package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class ClientOrderReceipt implements Serializable {
    private String
            id,
            ClientID,ResponsibleDistributorID,
            orderDate_Y,orderDate_H,
            lat_location,long_location,governorate;

            boolean isSupervisorConveyedTheOrderFromTheStoreToTheDistributor,
                    isDistributorReceivedTheOrderFromTheStore,

            isDeliveredByDistributorToClient,
            isReceivedByClient;
            //14

    public String getOrderDate_Y() {
        return orderDate_Y;
    }

    public void setOrderDate_Y(String orderDate_Y) {
        this.orderDate_Y = orderDate_Y;
    }

    public String getOrderDate_H() {
        return orderDate_H;
    }

    public void setOrderDate_H(String orderDate_H) {
        this.orderDate_H = orderDate_H;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClient_ID() {
        return ClientID;
    }

    public void setClient_ID(String client_ID) {
        ClientID = client_ID;
    }

    public String getResponsibleDistributorID() {
        return ResponsibleDistributorID;
    }

    public void setResponsibleDistributorID(String responsibleDistributorID) {
        ResponsibleDistributorID = responsibleDistributorID;
    }

    public String getLat_location() {
        return lat_location;
    }

    public void setLat_location(String lat_location) {
        this.lat_location = lat_location;
    }

    public String getLong_location() {
        return long_location;
    }

    public void setLong_location(String long_location) {
        this.long_location = long_location;
    }

    public String getGovernorate() {
        return governorate;
    }

    public void setGovernorate(String governorate) {
        this.governorate = governorate;
    }

    public boolean isSupervisorConveyedTheOrderFromTheStoreToTheDistributor() {
        return isSupervisorConveyedTheOrderFromTheStoreToTheDistributor;
    }

    public void setSupervisorConveyedTheOrderFromTheStoreToTheDistributor(boolean supervisorConveyedTheOrderFromTheStoreToTheDistributor) {
        isSupervisorConveyedTheOrderFromTheStoreToTheDistributor = supervisorConveyedTheOrderFromTheStoreToTheDistributor;
    }

    public boolean isDeliveredByDistributorToClient() {
        return isDeliveredByDistributorToClient;
    }

    public void setDeliveredByDistributorToClient(boolean deliveredByDistributorToClient) {
        isDeliveredByDistributorToClient = deliveredByDistributorToClient;
    }

    public boolean isReceivedByClient() {
        return isReceivedByClient;
    }

    public void setReceivedByClient(boolean receivedByClient) {
        isReceivedByClient = receivedByClient;
    }

    public boolean isDistributorReceivedTheOrderFromTheStore() {
        return isDistributorReceivedTheOrderFromTheStore;
    }

    public void setDistributorReceivedTheOrderFromTheStore(boolean distributorReceivedTheOrderFromTheStore) {
        isDistributorReceivedTheOrderFromTheStore = distributorReceivedTheOrderFromTheStore;
    }
}
