package com.buzzlegroup.buzzlemobile.data.model;

/**
 * Class that models service information from backend
 * (It's basically a struct)
 * @author Thomas Tran
 */
public class Service {

    //Storing all of these as strings to prevent conversion from and to String
    private final String id;
    private final String sellerID;
    private final String title;
    private final String sellerName;
    private final String date;
    private final String price;
    private final String status;

    public Service(String id, String sellerID, String title, String sellerName, String date, String price, String status) {
        this.id = id;
        this.sellerID = sellerID;
        this.title = title;
        this.sellerName = sellerName;
        this.date = date;
        this.price = price;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getSellerID() {
        return sellerID;
    }

    public String getTitle() {
        return title;
    }

    public String getSellerName() {
        return sellerName;
    }

    public String getDate() {
        return date;
    }

    public String getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }
}