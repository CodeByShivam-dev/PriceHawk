package com.pricehawk.dto;

/**
 * 📱 SmartphoneDTO — Data Transfer Object
 *
 * This class defines the structure of smartphone data
 * that will be sent as JSON to the frontend.
 *
 * 🧩 Purpose:
 *  - Used by Controller and Service layer for clean data exchange
 *  - Avoids sending raw maps or entities directly
 */

public class SmartphoneDTO
{

    private String store;
    private double price;
    private double rating;
    private boolean inStock;

    // 🧱 Default Constructor
    public SmartphoneDTO() {}

    // 🎯 Parameterized Constructor
    public SmartphoneDTO(String store, double price, double rating, boolean inStock) {
        this.store = store;
        this.price = price;
        this.rating = rating;
        this.inStock = inStock;
    }

    // ⚙️ Getters and Setters
    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
