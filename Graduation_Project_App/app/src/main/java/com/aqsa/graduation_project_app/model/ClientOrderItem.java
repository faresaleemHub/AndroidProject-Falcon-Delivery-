package com.aqsa.graduation_project_app.model;

public class ClientOrderItem {
    private String id,ClientOrderReceiptID,productID,productCategoryID,Quantity,buyPrice;

    public ClientOrderItem() {
    }

    public ClientOrderItem(String receipt_ID, String product_ID, String productCategory_ID, String quantity, String buyPrice) {
        ClientOrderReceiptID = receipt_ID;
        this.productID = product_ID;
        this.productCategoryID = productCategory_ID;
        Quantity = quantity;
        this.buyPrice = buyPrice;
    }

    public String getProductCategory_ID() {
        return productCategoryID;
    }

    public void setProductCategory_ID(String productCategory_ID) {
        this.productCategoryID = productCategory_ID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceipt_ID() {
        return ClientOrderReceiptID;
    }

    public void setReceipt_ID(String receipt_ID) {
        ClientOrderReceiptID = receipt_ID;
    }

    public String getProduct_ID() {
        return productID;
    }

    public void setProduct_ID(String product_ID) {
        this.productID = product_ID;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(String buyPrice) {
        this.buyPrice = buyPrice;
    }
}
