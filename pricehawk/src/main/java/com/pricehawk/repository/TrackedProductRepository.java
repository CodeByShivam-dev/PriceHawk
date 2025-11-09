package com.pricehawk.repository;

import com.pricehawk.entity.TrackedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 🧠 TrackedProductRepository
 *
 * Handles database operations for TrackedProduct entities.
 * (Add, find, delete tracked models, etc.)
 */

@Repository
public interface TrackedProductRepository extends JpaRepository<TrackedProduct, Long>
{
}
