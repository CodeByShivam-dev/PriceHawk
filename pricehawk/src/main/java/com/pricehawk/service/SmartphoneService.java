package com.pricehawk.service;

import com.pricehawk.dto.SmartphonePriceResult;
import com.pricehawk.entity.PriceSnapshot;
import com.pricehawk.entity.SearchHistory;
import com.pricehawk.repository.PhoneSpecsJpaRepository;
import com.pricehawk.repository.PriceSnapshotRepository;
import com.pricehawk.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Central service responsible for:
 * - collecting price data from multiple sources
 * - reusing recently cached results
 * - enriching results with specifications
 * - storing search analytics and price history
 */
@Service
public class SmartphoneService
{

    private static final Logger log =
            LoggerFactory.getLogger(SmartphoneService.class);

    private static final boolean DEMO_FALLBACK_ENABLED = true;

    private final Executor apiExecutor;
    private final PriceScraperService scraperService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final PhoneSpecsService phoneSpecsService;
    private final PhoneSpecsJpaRepository phoneSpecsRepository;

    public SmartphoneService(
            @Qualifier("apiExecutor") Executor apiExecutor,
            PriceScraperService scraperService,
            SearchHistoryRepository searchHistoryRepository,
            PriceSnapshotRepository priceSnapshotRepository,
            PhoneSpecsService phoneSpecsService,
            PhoneSpecsJpaRepository phoneSpecsRepository)
    {
        this.apiExecutor = apiExecutor;
        this.scraperService = scraperService;
        this.searchHistoryRepository = searchHistoryRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.phoneSpecsService = phoneSpecsService;
        this.phoneSpecsRepository = phoneSpecsRepository;
    }

    /**
     * Main search workflow:
     *
     * 1. Check recent cached results
     * 2. Scrape live sources in parallel
     * 3. Use fallback entries if nothing is found
     * 4. Attach specs information
     * 5. Store analytics/history asynchronously
     */
    public List<SmartphonePriceResult> fetchSmartphoneData(String query)
    {
        Instant now = Instant.now();
        Instant recentThreshold = now.minusSeconds(3 * 3600);

        String normalized =
                query.toLowerCase().trim();

        /*
         * Recently captured snapshots are reused to avoid
         * unnecessary scraping requests and improve response time.
         */
        List<PriceSnapshot> cached =
                priceSnapshotRepository
                        .findByModelNormalizedAndCapturedAtAfter(
                                normalized,
                                recentThreshold
                        );

        if (cached != null && !cached.isEmpty())
        {
            List<SmartphonePriceResult> cachedResults =
                    cached.stream()
                            .map(s -> SmartphonePriceResult.builder()
                                    .store(s.getStore())
                                    .title(s.getTitle())
                                    .price(
                                            s.getPrice() != null
                                                    ? s.getPrice().intValue()
                                                    : null
                                    )
                                    .imageUrl(s.getImageUrl())
                                    .productUrl(s.getProductUrl())
                                    .build())
                            .collect(Collectors.toList());

            injectSpecsAsync(query, cachedResults);

            return cachedResults;
        }

        /*
         * Amazon and Flipkart scraping run concurrently
         * to reduce overall request latency.
         */
        CompletableFuture<SmartphonePriceResult> fAmazon =
                CompletableFuture.supplyAsync(
                        () -> scraperService.scrapeAmazon(query).orElse(null),
                        apiExecutor
                );

        CompletableFuture<SmartphonePriceResult> fFlipkart =
                CompletableFuture.supplyAsync(
                        () -> scraperService.scrapeFlipkart(query).orElse(null),
                        apiExecutor
                );

        CompletableFuture.allOf(
                fAmazon,
                fFlipkart
        ).join();

        List<SmartphonePriceResult> results =
                Arrays.asList(fAmazon, fFlipkart)
                        .stream()
                        .map(f -> f.getNow(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        /*
         * Keeps the UI usable even when external sources
         * fail or temporarily block scraping requests.
         */
        if (results.isEmpty() && DEMO_FALLBACK_ENABLED)
        {
            String encoded =
                    URLEncoder.encode(
                            query,
                            StandardCharsets.UTF_8
                    );

            results = List.of(
                    SmartphonePriceResult.builder()
                            .store("Flipkart")
                            .title(query)
                            .productUrl(
                                    "https://www.flipkart.com/search?q="
                                            + encoded
                            )
                            .imageUrl(
                                    "https://placehold.co/300x300?text=Flipkart"
                            )
                            .build(),

                    SmartphonePriceResult.builder()
                            .store("Amazon")
                            .title(query)
                            .productUrl(
                                    "https://www.amazon.in/s?k="
                                            + encoded
                            )
                            .imageUrl(
                                    "https://placehold.co/300x300?text=Amazon"
                            )
                            .build()
            );
        }

        injectSpecsAsync(query, results);

        /*
         * Persistence work is intentionally moved off
         * the request thread because it is not required
         * for producing the response.
         */
        if (!results.isEmpty())
        {
            List<SmartphonePriceResult> finalResults =
                    results;

            CompletableFuture.runAsync(
                    () -> saveSnapshots(query, finalResults)
            );

            CompletableFuture.runAsync(
                    () -> saveHistory(query, finalResults.size())
            );
        }

        return results.stream()
                .sorted(
                        Comparator.comparing(
                                r -> r.getPrice() != null
                                        ? r.getPrice()
                                        : 999999,
                                Integer::compareTo
                        )
                )
                .collect(Collectors.toList());
    }

    /**
     * Adds specification information from local storage
     * and schedules background enrichment for the best result.
     */
    private void injectSpecsAsync(
            String query,
            List<SmartphonePriceResult> results)
    {
        if (results == null || results.isEmpty())
        {
            return;
        }

        for (SmartphonePriceResult r : results)
        {
            if (r.getSpecsSummary() == null
                    || r.getSpecsSummary().isBlank())
            {
                String model =
                        normalizeModel(
                                r.getTitle() != null
                                        ? r.getTitle()
                                        : query
                        );

                phoneSpecsRepository
                        .findByModelNormalized(model)
                        .ifPresent(
                                spec -> r.setSpecsSummary(
                                        spec.getSummary()
                                )
                        );
            }
        }

        Optional<SmartphonePriceResult> bestOpt =
                results.stream()
                        .filter(r -> r.getPrice() != null)
                        .min(
                                Comparator.comparingInt(
                                        SmartphonePriceResult::getPrice
                                )
                        );

        bestOpt.ifPresent(best ->
        {
            if (best.getSpecsSummary() == null
                    || best.getSpecsSummary().isBlank())
            {
                best.setSpecsSummary(
                        "8GB RAM · 256GB Storage · 5000mAh Battery · 5G"
                );
            }

            phoneSpecsService.enrichWithSpecs(
                    best.getTitle(),
                    best.getProductUrl(),
                    best
            );
        });
    }

    /**
     * Creates a consistent key for caching and lookup operations.
     */
    private String normalizeModel(String name)
    {
        if (name == null)
        {
            return "";
        }

        return name.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Stores a point-in-time price record
     * for future trend analysis.
     */
    private void saveSnapshots(
            String query,
            List<SmartphonePriceResult> results)
    {
        for (SmartphonePriceResult r : results)
        {
            PriceSnapshot snap =
                    new PriceSnapshot(
                            query,
                            r.getStore(),
                            r.getPrice(),
                            r.getProductUrl(),
                            r.getImageUrl(),
                            r.getTitle(),
                            r.getRating(),
                            r.getProductUrl() != null,
                            null
                    );

            snap.setCapturedAt(Instant.now());

            priceSnapshotRepository.save(snap);
        }
    }

    /**
     * Records search activity for analytics,
     * popularity tracking and future recommendations.
     */
    private void saveHistory(
            String query,
            int count)
    {
        searchHistoryRepository.save(
                new SearchHistory(
                        query,
                        count,
                        null
                )
        );
    }
}