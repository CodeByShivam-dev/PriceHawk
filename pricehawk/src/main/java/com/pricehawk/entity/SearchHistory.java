package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String query;
    private int resultsCount;
    private LocalDateTime searchedAt;

    public SearchHistory() {}

    public SearchHistory(String query, int resultsCount) {
        this.query = query;
        this.resultsCount = resultsCount;
        this.searchedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public int getResultsCount() { return resultsCount; }
    public void setResultsCount(int resultsCount) { this.resultsCount = resultsCount; }
    public LocalDateTime getSearchedAt() { return searchedAt; }
    public void setSearchedAt(LocalDateTime searchedAt) { this.searchedAt = searchedAt; }
}
