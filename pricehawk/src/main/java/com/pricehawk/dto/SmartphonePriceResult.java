package com.pricehawk.dto;

import java.time.Instant;

/**
 * Rich result returned by scrapers and service.
 */
public class SmartphonePriceResult {
    private String store;
    private Double price;
    private String productUrl;
    private String title;
    private boolean inStock;
    private Instant checkedAt;

    public SmartphonePriceResult() {}

    public SmartphonePriceResult(String store, Double price, String productUrl, String title, boolean inStock) {
        this.store = store;
        this.price = price;
        this.productUrl = productUrl;
        this.title = title;
        this.inStock = inStock;
        this.checkedAt = Instant.now();
    }

    // getters / setters
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
