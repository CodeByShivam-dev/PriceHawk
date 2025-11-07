package com.pricehawk.repository;

import com.pricehawk.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for time-series price snapshots.
 * Add common queries here—example provided.
 */
@Repository
public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    // find latest N snapshots for a model across stores (example)
    List<PriceSnapshot> findTop100ByModelNormalizedOrderByCapturedAtDesc(String modelNormalized);
}
