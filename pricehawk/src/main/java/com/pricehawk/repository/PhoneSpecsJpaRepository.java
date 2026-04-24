package com.pricehawk.repository;

import com.pricehawk.entity.PhoneSpecs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneSpecsJpaRepository extends JpaRepository<PhoneSpecs, Long>
{

    Optional<PhoneSpecs> findByModelNormalized(String modelNormalized);
}
