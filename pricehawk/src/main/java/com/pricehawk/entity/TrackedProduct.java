package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a product that a user is actively tracking (watchlist).
 *
 * This entity is primarily used by:
 * - background schedulers (to periodically re-check prices)
 * - notification logic (price drop alerts)
 *
 * Intentionally kept lightweight — no heavy relations yet.
 */
@Entity
@Table(name = "tracked_products")
public class TrackedProduct
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Display name of the product (kept as-is from source)
    private String productName;

    // Source platform (Flipkart, Amazon, etc.)
    private String store;

    // Last known price when this record was updated
    private Double currentPrice;

    /**
     * Optional threshold set by user.
     * If currentPrice <= targetPrice → eligible for notification.
     */
    @Column(name = "target_price")
    private Double targetPrice;

    // Direct link to product page (used in UI + notifications)
    @Column(name = "product_url", length = 1024)
    private String productUrl;

    // Thumbnail used for UI cards / alerts
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    /**
     * Placeholder for future user system.
     * Currently not enforced with FK to keep schema simple.
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * When tracking started.
     * Useful for debugging scheduler behavior and analytics.
     */
    @Column(name = "tracked_at")
    private Instant trackedAt;

    /**
     * Frequency (in hours) at which price should be rechecked.
     * Gives flexibility for future "smart tracking intervals".
     */
    @Column(name = "frequency_hours")
    private Integer frequencyHours;

    public TrackedProduct()
    {
    }

    /**
     * Main constructor used from service layer.
     * Sets tracking timestamp automatically.
     */
    public TrackedProduct(String productName,
                          String store,
                          Double currentPrice,
                          Double targetPrice,
                          String productUrl,
                          String imageUrl,
                          Long userId,
                          Integer frequencyHours)
    {
        this.productName = productName;
        this.store = store;
        this.currentPrice = currentPrice;
        this.targetPrice = targetPrice;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.frequencyHours = frequencyHours;

        // capture creation moment (not relying on DB default)
        this.trackedAt = Instant.now();
    }

    // --- Getters / Setters ---

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

    public Double getTargetPrice()
    {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice)
    {
        this.targetPrice = targetPrice;
    }

    public String getProductUrl()
    {
        return productUrl;
    }

    public void setProductUrl(String productUrl)
    {
        this.productUrl = productUrl;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Instant getTrackedAt()
    {
        return trackedAt;
    }

    public void setTrackedAt(Instant trackedAt)
    {
        this.trackedAt = trackedAt;
    }

    public Integer getFrequencyHours()
    {
        return frequencyHours;
    }

    public void setFrequencyHours(Integer frequencyHours)
    {
        this.frequencyHours = frequencyHours;
    }
}