package com.pricehawk.repository;

import com.pricehawk.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SearchHistory. We keep it simple for now; advanced queries
 * (top searches, timeframe queries) can be added as custom repository methods or
 * using @Query when needed.
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long>
{
    // Example extension methods you can add later:
    // List<SearchHistory> findTop100ByQueryNormalizedOrderBySearchedAtDesc(String q);
}
