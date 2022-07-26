package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

// Receipt =>CompanyorderReceipt

public class Receipt implements Serializable {
    String id,
            MerchantID,
            orderDate_H,
            orderDate_Y;
    boolean isPriced,
            isAcceptedBySupervisor,
            isSentByMerchant,
            isReceivedBySupervisor,
            isDoneByMerchant;

    public Receipt(String merchantID, String dateRegister_H, String dateRegister_Y, boolean isPriced, boolean isAcceptedBySupervisor, boolean isSentByMerchant, boolean isReceivedBySupervisor, boolean isDoneSentByMerchant, boolean isDoneByMerchant) {
        MerchantID = merchantID;
        orderDate_H = dateRegister_H;
        orderDate_Y = dateRegister_Y;
        this.isPriced = isPriced;
        this.isAcceptedBySupervisor = isAcceptedBySupervisor;
        this.isSentByMerchant = isSentByMerchant;
        this.isReceivedBySupervisor = isReceivedBySupervisor;
        this.isDoneByMerchant = isDoneByMerchant;
    }

    public Receipt() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMerchantID() {
        return MerchantID;
    }

    public void setMerchantID(String merchantID) {
        MerchantID = merchantID;
    }

    public String getDateRegister_H() {
        return orderDate_H;
    }

    public void setDateRegister_H(String dateRegister_H) {
        orderDate_H = dateRegister_H;
    }

    public String getDateRegister_Y() {
        return orderDate_Y;
    }

    public void setDateRegister_Y(String dateRegister_Y) {
        orderDate_Y = dateRegister_Y;
    }

    public boolean isPriced() {
        return isPriced;
    }

    public void setPriced(boolean priced) {
        isPriced = priced;
    }

    public boolean isAcceptedBySupervisor() {
        return isAcceptedBySupervisor;
    }

    public void setAcceptedBySupervisor(boolean acceptedBySupervisor) {
        isAcceptedBySupervisor = acceptedBySupervisor;
    }

    public boolean isSentByMerchant() {
        return isSentByMerchant;
    }

    public void setSentByMerchant(boolean sentByMerchant) {
        isSentByMerchant = sentByMerchant;
    }

    public boolean isReceivedBySupervisor() {
        return isReceivedBySupervisor;
    }

    public void setReceivedBySupervisor(boolean receivedBySupervisor) {
        isReceivedBySupervisor = receivedBySupervisor;
    }


    public boolean isDoneByMerchant() {
        return isDoneByMerchant;
    }

    public void setDoneByMerchant(boolean doneByMerchant) {
        isDoneByMerchant = doneByMerchant;
    }
}