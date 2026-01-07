package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.Instant;
/**
 * Represents a normalized smartphone model's specs summary.
 * Stores lightweight specs info fetched from external sources.
 */
@Entity
@Table(
        name = "phone_specs",
        indexes = {
                @Index(name = "idx_specs_model_norm", columnList = "model_normalized")
        }
)
public class PhoneSpecs
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;// Primary key, auto-generated


    @Column(name = "model_normalized", nullable = false, length = 256, unique = true)
    private String modelNormalized; // e.g., "iphone 15 128gb", lowercase normalized

    // Compact specs summary text
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;  // Concise text summary of specs: RAM, storage, display, etc.

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated; // When this record was last refreshed

    public PhoneSpecs() {}

    // Constructor to quickly create a new specs record
    public PhoneSpecs(String modelNormalized, String summary)
    {
        this.modelNormalized = modelNormalized;
        this.summary = summary;
        this.lastUpdated = Instant.now();
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

    public String getSummary()
    {
        return summary;
    }
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Instant getLastUpdated()
    {
        return lastUpdated;
    }
    public void setLastUpdated(Instant lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }
}
