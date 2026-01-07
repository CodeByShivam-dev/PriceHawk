package com.pricehawk.repository;

import com.pricehawk.entity.TrackedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TrackedProduct (watchlist/favorites).
 */
@Repository
public interface TrackedProductRepository extends JpaRepository<TrackedProduct, Long>
{
    List<TrackedProduct> findByUserId(Long userId);
}
