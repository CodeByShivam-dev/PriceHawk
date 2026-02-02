package com.pricehawk.service;

import com.pricehawk.entity.PhoneSpecs;
import com.pricehawk.repository.PhoneSpecsJpaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Database-backed implementation of PhoneSpecsRepository.
 *
 * Acts as a thin adapter layer between the domain service
 * and the underlying JPA repository to keep persistence concerns isolated.
 */
@Service
public class DbPhoneSpecsRepository implements PhoneSpecsService.PhoneSpecsRepository
{

    private final PhoneSpecsJpaRepository jpa;

    public DbPhoneSpecsRepository(PhoneSpecsJpaRepository jpa)
    {
        this.jpa = jpa;
    }

    /**
     * Fetches the cached specs summary for a normalized model name.
     * Used to avoid unnecessary re-scraping when data is already available.
     */
    @Override
    public Optional<String> findSummaryByModelNormalized(String modelNormalized)
    {
        return jpa.findByModelNormalized(modelNormalized)
                .map(PhoneSpecs::getSummary);
    }

    /**
     * Upserts specs data for a phone model.
     *
     * If the record already exists, only the summary and timestamp are refreshed.
     * This keeps historical identity stable while allowing content updates.
     */
    @Override
    public void saveOrUpdate(String modelNormalized, String summary)
    {
        PhoneSpecs entity = jpa.findByModelNormalized(modelNormalized)
                .orElseGet(() -> new PhoneSpecs(modelNormalized, summary));

        entity.setSummary(summary);
        entity.setLastUpdated(Instant.now());

        jpa.save(entity);
    }
}
