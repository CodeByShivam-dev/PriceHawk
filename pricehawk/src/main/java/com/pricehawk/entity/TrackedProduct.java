package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 🧩 TrackedProduct Entity
 *
 * 🧠 Purpose:
 *  Represents a product that a user wants to track over time.
 *  The scheduler will monitor this entry for price changes and
 *  automatically update PriceSnapshot whenever new data is fetched.
 *
 * 🏗️ Structure:
 *  id → Primary Key
 *  productName → Name of smartphone
 *  store → E-commerce platform (Amazon, Flipkart, etc.)
 *  currentPrice → Latest price at time of tracking
 *  trackedAt → When tracking started
 */

@Entity
@Table(name = "tracked_products")
public class TrackedProduct
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private String store;
    private Double currentPrice;
    private LocalDateTime trackedAt;

    // ✅ Constructor
    public TrackedProduct()
    {}

    public TrackedProduct(String productName, String store, Double currentPrice)
    {
        this.productName = productName;
        this.store = store;
        this.currentPrice = currentPrice;
        this.trackedAt = LocalDateTime.now();
    }

    // ✅ Getters and Setters
    public Long getId()
    {
        return id;
    }

    public String getProductName()
    {
        return productName;
    }
    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getStore()
    {
        return store;
    }
    public void setStore(String store)
    {
        this.store = store;
    }

    public Double getCurrentPrice()
    {
        return currentPrice;
    }
    public void setCurrentPrice(Double currentPrice)
    {
        this.currentPrice = currentPrice;
    }

    public LocalDateTime getTrackedAt()
    {
        return trackedAt;
    }
    public void setTrackedAt(LocalDateTime trackedAt)
    {
        this.trackedAt = trackedAt;
    }
}
