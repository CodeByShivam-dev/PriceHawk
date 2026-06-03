package com.pricehawk.dto;

import lombok.Data;

/**
 * Unified response model used across the pricing pipeline.
 *
 * Every scraper (Amazon, Flipkart, Croma, future providers, etc.)
 * converts its raw output into this DTO so the aggregation and API
 * layers can work with a single consistent structure.
 */
@Data
public class SmartphonePriceResult
{

    private String store;
    private String title;

    /*
     * Prices are stored as Integer because the application currently
     * works with whole INR values only. This also keeps sorting,
     * comparisons and API payloads straightforward.
     */
    private Integer price;

    /*
     * Condensed specification text shown in result cards.
     * Example:
     * "8GB RAM · 256GB Storage · Snapdragon 8 Gen 3"
     */
    private String specsSummary;

    private String imageUrl;
    private String productUrl;

    /*
     * Not every marketplace exposes ratings consistently,
     * therefore this field is intentionally nullable.
     */
    private Double rating;

    /*
     * Nullable by design.
     *
     * true  -> confirmed in stock
     * false -> confirmed out of stock
     * null  -> availability could not be determined
     */
    private Boolean inStock;

    public SmartphonePriceResult()
    {
    }

    /**
     * Constructor primarily used by scraper implementations.
     *
     * Scrapers usually extract prices as Double values, while the rest
     * of the system consumes normalized integer prices.
     */
    public SmartphonePriceResult(
            String store,
            Double price,
            String productUrl,
            String title,
            boolean inStock,
            String imageUrl,
            Double rating
    )
    {
        this.store = store;
        this.title = title;
        this.price = price != null ? price.intValue() : null;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.inStock = inStock;
    }

    /**
     * Legacy constructor retained temporarily for older scraper
     * implementations that have not yet been migrated.
     *
     * Safe to remove once all call sites use the primary constructor
     * or builder API.
     */
    public SmartphonePriceResult(
            String store,
            Double price,
            String productUrl,
            String title,
            boolean inStock,
            String imageUrl,
            Double rating,
            Object ignored
    )
    {
    }

    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Manual builder kept intentionally instead of Lombok @Builder.
     *
     * This gives explicit control over object creation and avoids
     * generated builder code becoming part of the public contract.
     */
    public static class Builder
    {

        private String store;
        private String title;
        private Integer price;
        private String specsSummary;
        private String imageUrl;
        private String productUrl;
        private Double rating;
        private Boolean inStock;

        public Builder store(String store)
        {
            this.store = store;
            return this;
        }

        public Builder title(String title)
        {
            this.title = title;
            return this;
        }

        public Builder price(Integer price)
        {
            this.price = price;
            return this;
        }

        public Builder specsSummary(String specsSummary)
        {
            this.specsSummary = specsSummary;
            return this;
        }

        public Builder imageUrl(String imageUrl)
        {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder productUrl(String productUrl)
        {
            this.productUrl = productUrl;
            return this;
        }

        public Builder rating(Double rating)
        {
            this.rating = rating;
            return this;
        }

        public Builder inStock(Boolean inStock)
        {
            this.inStock = inStock;
            return this;
        }

        public SmartphonePriceResult build()
        {
            SmartphonePriceResult result = new SmartphonePriceResult();

            result.store = this.store;
            result.title = this.title;
            result.price = this.price;
            result.specsSummary = this.specsSummary;
            result.imageUrl = this.imageUrl;
            result.productUrl = this.productUrl;
            result.rating = this.rating;
            result.inStock = this.inStock;

            return result;
        }
    }
}