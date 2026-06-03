package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Stores each search request made by the user.
 *
 * Enables:
 * - trending queries
 * - identifying low-result searches
 * - future personalization / recommendations
 */
@Entity
@Table(
        name = "search_history",
        indexes = {
                // used for grouping + ranking popular searches
                @Index(name = "idx_search_query_norm", columnList = "query_normalized"),

                // supports time-based queries (recent activity, trends)
                @Index(name = "idx_search_searched_at", columnList = "searched_at")
        }
)
public class SearchHistory
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // raw user input (kept unchanged for UI/debugging)
    @Column(name = "query", nullable = false, length = 512)
    private String query;

    /**
     * normalized form used internally for grouping/search.
     * avoids duplication caused by casing/spacing differences.
     */
    @Column(name = "query_normalized", nullable = false, length = 512)
    private String queryNormalized;

    /**
     * number of results returned for this search.
     * helps detect weak queries or scraper failures.
     */
    @Column(name = "results_count", nullable = false)
    private Integer resultsCount;

    // reserved for future user-specific analytics
    @Column(name = "user_id")
    private Long userId;

    // capture time of the search (not user-provided)
    @Column(name = "searched_at", nullable = false, updatable = false)
    private Instant searchedAt;

    public SearchHistory()
    {
    }

    /**
     * Main constructor used from service layer.
     * normalization happens here to enforce consistency at write-time.
     */
    public SearchHistory(String query, Integer resultsCount, Long userId)
    {
        this.query = query;
        this.queryNormalized = normalize(query);
        this.resultsCount = resultsCount;
        this.userId = userId;
        this.searchedAt = Instant.now();
    }

    // lightweight normalization (kept simple intentionally)
    private String normalize(String q)
    {
        return q == null ? "" : q.trim().toLowerCase();
    }

    // --- Getters & Setters ---

    public Long getId()
    {
        return id;
    }

    public String getQuery()
    {
        return query;
    }

    /**
     * ensures normalized field stays in sync if query changes
     */
    public void setQuery(String query)
    {
        this.query = query;
        this.queryNormalized = normalize(query);
    }

    public String getQueryNormalized()
    {
        return queryNormalized;
    }

    public Integer getResultsCount()
    {
        return resultsCount;
    }

    public void setResultsCount(Integer resultsCount)
    {
        this.resultsCount = resultsCount;
    }

    public Instant getSearchedAt()
    {
        return searchedAt;
    }

    public void setSearchedAt(Instant searchedAt)
    {
        this.searchedAt = searchedAt;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
}