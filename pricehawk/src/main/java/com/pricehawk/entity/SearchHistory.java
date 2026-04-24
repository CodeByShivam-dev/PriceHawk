package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Stores each search request made by the user.
 *
 * Useful for:
 * - tracking popular searches
 * - debugging low-result queries
 * - future features like recommendations / trends
 */
@Entity
@Table(
        name = "search_history",
        indexes = {
                // speeds up queries like "top searched keywords"
                @Index(name = "idx_search_query_norm", columnList = "query_normalized"),

                // used for time-based analytics (recent searches, trends)
                @Index(name = "idx_search_searched_at", columnList = "searched_at")
        }
)
public class SearchHistory
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // original query as entered by user (kept for display/debugging)
    @Column(name = "query", nullable = false, length = 512)
    private String query;

    /**
     * normalized version used for consistent lookups and grouping.
     * avoids duplicates like "iPhone 15" vs "iphone 15".
     */
    @Column(name = "query_normalized", nullable = false, length = 512)
    private String queryNormalized;

    // number of results returned for this search (helps identify weak queries)
    @Column(name = "results_count", nullable = false)
    private Integer resultsCount;

    // optional: will be useful once user accounts / personalization is added
    @Column(name = "user_id")
    private Long userId;

    // timestamp of when search happened (used for trends / ordering)
    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt;

    public SearchHistory()
    {
    }

    /**
     * Main constructor used from service layer.
     * normalization is applied here to keep data consistent.
     */
    public SearchHistory(String query, Integer resultsCount, Long userId)
    {
        this.query = query;
        this.queryNormalized = normalize(query);
        this.resultsCount = resultsCount;
        this.userId = userId;
        this.searchedAt = Instant.now();
    }

    // simple normalization (kept lightweight intentionally)
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
     * keep normalized value in sync whenever query changes
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