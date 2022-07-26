package com.aqsa.graduation_project_app.model;

import java.io.Serializable;

public class PurchaseItem implements Serializable {
        String id,ProductID,
                Quantity,
                ReceiptID,
                price;

    public PurchaseItem(String productID, String quantity, String receiptID, String price) {
        ProductID = productID;
        Quantity = quantity;
        ReceiptID = receiptID;
        this.price = price;
    }

    public PurchaseItem() {
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getReceiptID() {
        return ReceiptID;
    }

    public void setReceiptID(String receiptID) {
        ReceiptID = receiptID;
    }
}
