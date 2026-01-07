package com.pricehawk.repository;

import com.pricehawk.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for time-series price snapshots.
 */
@Repository
public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long>
{

    List<PriceSnapshot> findTop100ByModelNormalizedOrderByCapturedAtDesc(String modelNormalized);


    List<PriceSnapshot> findByModelNormalizedAndCapturedAtAfter(String modelNormalized, Instant capturedAfter);
}
