package com.pricehawk.dto;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) representing the price information
 * of a smartphone from a single e-commerce store.
 *
 * Design intent:
 * - This class is intentionally kept free of persistence annotations.
 * - It acts as a clean contract between:
 *      → scraping layer
 *      → service layer
 *      → API responses / frontend consumption
 *
 * Keeping this DTO stable allows internal implementations
 * (scrapers, database schema, notification logic) to evolve
 * without breaking API consumers.
 */
public class SmartphonePriceResult
{

    private String store;        // Store name: Flipkart, Amazon, Croma, etc.
    private Double price;        // Current listed price
    private String productUrl;   // Direct link to the product page
    private String title;        // Product title/name as displayed
    private boolean inStock;     // Is the product in stock?
    private Instant checkedAt;   // When was this data fetched/scraped?
    private String imageUrl;     // URL of product image/thumbnail
    private Double rating;       // Optional: product's star rating (if available)
    private String specsSummary; // Short specs summary (RAM, storage, etc.)

    public SmartphonePriceResult()
    {
    }
    /**
     * Primary constructor used by scraping services
     * when full product data is available.
     *
     * Automatically sets the fetch timestamp to current time.
     */
    public SmartphonePriceResult(String store,
                                 Double price,
                                 String productUrl,
                                 String title,
                                 boolean inStock,
                                 String imageUrl,
                                 Double rating,
                                 String specsSummary)
    {
        this.store = store;
        this.price = price;
        this.productUrl = productUrl;
        this.title = title;
        this.inStock = inStock;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.specsSummary = specsSummary;
        this.checkedAt = Instant.now();
    }
    /**
     * Lightweight constructor for stores
     * that do not expose all metadata.
     *
     * Allows partial results instead of failing the entire response.
     */
    public SmartphonePriceResult(String store,
                                 Double price,
                                 String productUrl,
                                 String title,
                                 boolean inStock, String imageUrl, Double rating)
    {
        this(store, price, productUrl, title, inStock, null, null, null);
    }

    // --- Getters & Setters ---

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

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock)
    {
        this.inStock = inStock;
    }

    public Instant getCheckedAt()
    {
        return checkedAt;
    }

    public void setCheckedAt(Instant checkedAt)
    {
        this.checkedAt = checkedAt;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public Double getRating()
    {
        return rating;
    }

    public void setRating(Double rating)
    {
        this.rating = rating;
    }

    public String getSpecsSummary()
    {
        return specsSummary;
    }

    public void setSpecsSummary(String specsSummary)
    {
        this.specsSummary = specsSummary;
    }
}
