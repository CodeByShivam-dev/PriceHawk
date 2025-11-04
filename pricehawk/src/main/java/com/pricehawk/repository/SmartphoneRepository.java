package com.pricehawk.repository;

import com.pricehawk.model.SmartphoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 📦 SmartphoneRepository — Data Access Layer
 *
 * This interface handles all database operations for SmartphoneEntity.
 * Spring Data JPA automatically generates common queries (findAll, save, deleteById, etc.)
 */

@Repository
interface SmartphoneRepository extends JpaRepository<SmartphoneEntity, Long>
{
    // Custom queries can be added later (e.g., findByBrand, findByPriceBetween, etc.)
}
