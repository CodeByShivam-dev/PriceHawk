package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a single price capture for a product at a specific time.
 *
 * This entity is intentionally append-only (no updates),
 * so we can track price history, trends, and trigger alerts later.
 */
@Entity
@Table(
        name = "price_snapshot",
        indexes = {
                // helps queries like: latest price per product/store
                @Index(name = "idx_snapshot_model_store", columnList = "model_normalized, store"),

                // used for time-based filtering (cache / analytics)
                @Index(name = "idx_snapshot_captured_at", columnList = "captured_at")
        }
)
public class PriceSnapshot
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Normalized version of model name (lowercase, trimmed)
     * to avoid duplicates caused by inconsistent input formats.
     */
    @Column(name = "model_normalized", nullable = false, length = 256)
    private String modelNormalized;

    @Column(name = "store", nullable = false, length = 128)
    private String store;

    /**
     * Can be null when scraper fails to extract price
     * but we still want to store link-level fallback data.
     */
    @Column(name = "price")
    private Double price;

    @Column(name = "product_url", length = 1024)
    private String productUrl;

    // cached to avoid re-fetching images during listing
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    // raw title from source site (kept as-is for UI/debugging)
    @Column(name = "title", length = 256)
    private String title;

    @Column(name = "rating")
    private Double rating;

    // nullable because not all sources expose stock reliably
    @Column(name = "in_stock")
    private Boolean inStock;

    // reserved for future user-tracking / alerts feature
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    public PriceSnapshot()
    {
    }

    /**
     * Primary constructor used by scraper/service layer.
     * Normalization is enforced here to keep DB consistent.
     */
    public PriceSnapshot(String model,
                         String store,
                         Double price,
                         String productUrl,
                         String imageUrl,
                         String title,
                         Double rating,
                         Boolean inStock,
                         Long userId)
    {
        this.modelNormalized = normalize(model);
        this.store = store;
        this.price = price;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.title = title;
        this.rating = rating;
        this.inStock = inStock;
        this.userId = userId;
        this.capturedAt = Instant.now();
    }

    /**
     * Lightweight constructor for minimal snapshot creation.
     * Delegates to main constructor for consistency.
     */
    public PriceSnapshot(String model, String store, Double price, String productUrl)
    {
        this(model, store, price, productUrl, null, null, null, null, null);
    }

    /**
     * ⚠️ This constructor looks unused and does nothing.
     * Either implement properly or remove to avoid confusion.
     */
    public PriceSnapshot(String query,
                         String store,
                         Integer price,
                         String productUrl,
                         String imageUrl,
                         String title,
                         Double rating,
                         boolean inStock,
                         Object userId)
    {
    }

    // central normalization logic (kept simple on purpose)
    private String normalize(String s)
    {
        return s == null ? "" : s.trim().toLowerCase();
    }

    // --- Getters & Setters ---

    public Long getId()
    {
        return id;
    }

    public String getModelNormalized()
    {
        return modelNormalized;
    }

    public void setModelNormalized(String modelNormalized)
    {
        this.modelNormalized = modelNormalized;
    }

    public String getStore()
    {
        return store;
    }

    public void setStore(String store)
    {
        this.store = store;
    }

    public Double getPrice()
    {
        return price;
    }

    public void setPrice(Double price)
    {
        this.price = price;
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

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Double getRating()
    {
        return rating;
    }

    public void setRating(Double rating)
    {
        this.rating = rating;
    }

    public Boolean getInStock()
    {
        return inStock;
    }

    public void setInStock(Boolean inStock)
    {
        this.inStock = inStock;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Instant getCapturedAt()
    {
        return capturedAt;
    }

    public void setCapturedAt(Instant capturedAt)
    {
        this.capturedAt = capturedAt;
    }
}