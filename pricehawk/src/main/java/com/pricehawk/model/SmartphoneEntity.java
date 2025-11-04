package com.pricehawk.model;

import jakarta.persistence.*;

/**
 * 🧩 SmartphoneEntity — Database Model Class
 *
 * This class represents a single smartphone record in the database.
 * Each field here will become a column in the table.
 *
 * JPA (Java Persistence API) is used to automatically map this class to the database table.
 */

@Entity                     // Marks this class as a JPA Entity (table)
@Table(name = "smartphones") // Table name in DB
public class SmartphoneEntity
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto-increment primary key (ID)
    private Long id;

    @Column(nullable = false)
    private String name;  // Smartphone name (e.g., iPhone 15 Pro)

    @Column(nullable = false)
    private String brand; // Brand name (e.g., Apple, Samsung)

    private Double price; // Current price
    private Double rating; // Average rating (out of 5)
    private String source; // Source site (Amazon, Flipkart, etc.)
    private String productUrl; // Product page link

    // ✅ Default constructor (required by JPA)
    public SmartphoneEntity()
    {
    }

    // ✅ Parameterized constructor for convenience
    public SmartphoneEntity(String name, String brand, Double price, Double rating, String source, String productUrl) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
        this.source = source;
        this.productUrl = productUrl;
    }

    // ✅ Getters & Setters
    public Long getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getBrand()
    {
        return brand;
    }
    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public Double getPrice()
    {
        return price;
    }
    public void setPrice(Double price)
    {
        this.price = price;
    }

    public Double getRating()
    {
        return rating;
    }
    public void setRating(Double rating)
    {
        this.rating = rating;
    }

    public String getSource()
    {
        return source;
    }
    public void setSource(String source)
    {
        this.source = source;
    }

    public String getProductUrl()
    {
        return productUrl;
    }
    public void setProductUrl(String productUrl)
    {
        this.productUrl = productUrl;
    }
}
