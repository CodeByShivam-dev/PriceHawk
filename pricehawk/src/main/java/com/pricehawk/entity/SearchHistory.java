package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * SearchHistory
 *
 * Stores one record per user search. This table is used for analytics,
 * trending queries, and debugging. We keep only the minimal fields needed
 * foranalytics + fast queries.
 *
 * Design notes:
 * - searchedAt: timestamp when the search happened.
 * - queryNormalized: lowercase trimmed version of the query for grouping/indexing.
 * - resultsCount: number of results returned (helps detect partial/failure cases).
 *
 * Table/index choices:
 * - Index on (query_normalized) to speed up "top searched" queries.
 * - Index on (searchedAt) to quickly filter recent searches.
 */
@Entity
@Table(name = "search_history",
        indexes = {
                @Index(name = "idx_search_query_norm", columnList = "query_normalized"),
                @Index(name = "idx_search_searched_at", columnList = "searched_at")
        })
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Raw user query as typed (kept for debugging / UX)
     */
    @Column(name = "query", nullable = false, length = 512)
    private String query;

    /**
     * Normalized query (lowercase + trimmed) used for grouping/aggregation.
     */
    @Column(name = "query_normalized", nullable = false, length = 512)
    private String queryNormalized;

    /**
     * Number of result items returned by the comparison service.
     */
    @Column(name = "results_count", nullable = false)
    private Integer resultsCount;

    /**
     * When the search was executed.
     */
    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    public SearchHistory() {
        // JPA
    }

    public SearchHistory(String query, Integer resultsCount) {
        this.query = query;
        this.queryNormalized = normalize(query);
        this.resultsCount = resultsCount;
        this.searchedAt = LocalDateTime.now();
    }

    private String normalize(String q) {
        return q == null ? "" : q.trim().toLowerCase();
    }

    // --- getters & setters ---
    public Long getId() { return id; }
    public String getQuery() { return query; }
    public void setQuery(String query) {
        this.query = query;
        this.queryNormalized = normalize(query);
    }
    public String getQueryNormalized() { return queryNormalized; }
    public Integer getResultsCount() { return resultsCount; }
    public void setResultsCount(Integer resultsCount) { this.resultsCount = resultsCount; }
    public LocalDateTime getSearchedAt() { return searchedAt; }
    public void setSearchedAt(LocalDateTime searchedAt) { this.searchedAt = searchedAt; }
}
