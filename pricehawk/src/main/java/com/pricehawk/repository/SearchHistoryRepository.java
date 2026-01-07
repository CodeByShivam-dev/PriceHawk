package com.pricehawk.repository;

import com.pricehawk.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SearchHistory.
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}



