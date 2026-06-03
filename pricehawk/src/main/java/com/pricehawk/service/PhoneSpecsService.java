package com.pricehawk.service;

import com.pricehawk.dto.SmartphonePriceResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enriches search results with device specifications.
 *
 * Pricing data is considered the primary response, while specifications
 * are treated as supplemental metadata. Because of that, spec collection
 * is designed to fail independently without affecting the search flow.
 */
@Service
@Slf4j
public class PhoneSpecsService
{

    private final PhoneSpecsRepository specsRepository;
    private final SpecsScraperService specsScraper;

    /*
     * A dedicated worker pool prevents slower product-page scraping
     * from increasing API response times.
     */
    private final ExecutorService executor;

    public PhoneSpecsService(
            PhoneSpecsRepository specsRepository,
            SpecsScraperService specsScraper
    )
    {
        this.specsRepository = specsRepository;
        this.specsScraper = specsScraper;
        this.executor = Executors.newFixedThreadPool(4);
    }

    /**
     * Attempts to enrich the selected result with specification data.
     *
     * Strategy:
     * - Use cached data immediately if available.
     * - Trigger a background refresh to keep stored specs current.
     * - Never fail the main search request because of spec extraction.
     */
    public void enrichWithSpecs(
            String modelName,
            String productUrl,
            SmartphonePriceResult bestDeal
    )
    {
        if (bestDeal == null)
        {
            return;
        }

        String normalizedModel = normalize(modelName);

        try
        {
            Optional<String> fromDb =
                    specsRepository.findSummaryByModelNormalized(normalizedModel);

            fromDb.ifPresent(specs ->
            {
                if (bestDeal.getSpecsSummary() == null
                        || bestDeal.getSpecsSummary().isBlank())
                {
                    bestDeal.setSpecsSummary(specs);
                }
            });

            /*
             * Spec extraction is intentionally asynchronous because
             * marketplace product pages are significantly slower than
             * local database lookups.
             */
            executor.submit(() ->
            {
                try
                {
                    String scraped =
                            specsScraper.scrapeSpecs(productUrl);

                    if (scraped != null && !scraped.isBlank())
                    {
                        specsRepository.saveOrUpdate(
                                normalizedModel,
                                scraped
                        );
                    }
                }
                catch (Exception e)
                {
                    /*
                     * Missing specs should not impact the primary
                     * price-comparison experience.
                     */
                    log.warn(
                            "Specs scraping failed for model={}, url={}",
                            normalizedModel,
                            productUrl,
                            e
                    );
                }
            });
        }
        catch (Exception ex)
        {
            /*
             * Any enrichment failure is isolated from the search flow.
             */
            log.warn(
                    "PhoneSpecsService.enrichWithSpecs failed for model={}",
                    normalizedModel,
                    ex
            );
        }
    }

    /*
     * Produces a stable key for cache lookups and persistence,
     * regardless of input casing or accidental whitespace.
     */
    private String normalize(String s)
    {
        return s == null
                ? ""
                : s.trim().toLowerCase();
    }

    /**
     * Legacy integration retained for compatibility while older
     * API-based flows are being phased out.
     *
     * New code should not depend on this method.
     */
    @Deprecated
    public Optional<String> fetchSpecsSummaryFromExternalApi(String query)
    {
        log.info(
                "fetchSpecsSummaryFromExternalApi is disabled for query={}",
                query
        );

        return Optional.empty();
    }

    /*
     * Legacy DTOs preserved because some historical integrations
     * still reference the original API response structure.
     */

    @Data
    public static class SearchResponse
    {
        public SearchData data;
    }

    @Data
    public static class SearchData
    {
        public java.util.List<PhoneItem> phones;
    }

    @Data
    public static class PhoneItem
    {
        public String phone_name;
        public String slug;
    }

    @Data
    public static class DetailsResponse
    {
        public PhoneData data;
    }

    @Data
    public static class PhoneData
    {
        public java.util.Map<String, java.util.List<SpecItem>> specs;
    }

    @Data
    public static class SpecItem
    {
        public String key;
        public java.util.List<String> val;
    }

    @Data
    public static class PhoneDetails
    {
        public String display;
        public String chipset;
        public String memory;
        public String mainCamera;
        public String battery;
    }

    /**
     * Storage abstraction for specification summaries.
     *
     * The service remains independent from the persistence layer,
     * allowing implementations backed by JPA, Redis, or other stores.
     */
    public interface PhoneSpecsRepository
    {
        Optional<String> findSummaryByModelNormalized(
                String modelNormalized
        );

        void saveOrUpdate(
                String modelNormalized,
                String summary
        );
    }

    /**
     * Contract for extracting specifications from product pages.
     *
     * Implementations may rely on Jsoup, Selenium, APIs,
     * or any other retrieval mechanism.
     */
    public interface SpecsScraperService
    {
        String scrapeSpecs(String productUrl);
    }
}