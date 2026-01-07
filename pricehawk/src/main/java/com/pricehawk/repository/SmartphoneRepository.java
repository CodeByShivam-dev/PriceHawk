package com.pricehawk.repository;

import com.pricehawk.model.SmartphoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for static smartphone master data (if used).
 */
@Repository
public interface SmartphoneRepository extends JpaRepository<SmartphoneEntity, Long>
{

}
