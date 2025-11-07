package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PriceSnapshot
 *
 * Stores periodic captures of a single product on a single store.
 * Use this table for time-series analysis, trend detection, and price-drop alerts.
 *
 * Typical usage:
 * - A scheduler (every 6/12 hours) fetches current prices for tracked models
 *   and inserts PriceSnapshot rows for each store/product.
 *
 * Important:
 * - Keep rows compact. Avoid storing large JSON blobs here.
 */
@Entity
@Table(name = "price_snapshot",
        indexes = {
                @Index(name = "idx_snapshot_model_store", columnList = "model_normalized, store"),
                @Index(name = "idx_snapshot_captured_at", columnList = "captured_at")
        })
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Normalized product/model identifier (e.g., "iphone 15 pro").
     * Normalize so grouping & range queries are fast.
     */
    @Column(name = "model_normalized", nullable = false, length = 256)
    private String modelNormalized;

    @Column(name = "store", nullable = false, length = 128)
    private String store;

    @Column(name = "price", nullable = false)
    private Double price;

    /**
     * Optional product url to allow redirection from stored snapshots
     * (can be null for snapshots without URL).
     */
    @Column(name = "product_url", length = 1024)
    private String productUrl;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    public PriceSnapshot() { }

    public PriceSnapshot(String model, String store, Double price, String productUrl) {
        this.modelNormalized = normalize(model);
        this.store = store;
        this.price = price;
        this.productUrl = productUrl;
        this.capturedAt = LocalDateTime.now();
    }

    private String normalize(String s) { return s == null ? "" : s.trim().toLowerCase(); }

    // getters / setters
    public Long getId() { return id; }
    public String getModelNormalized() { return modelNormalized; }
    public void setModelNormalized(String modelNormalized) { this.modelNormalized = modelNormalized; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
}
