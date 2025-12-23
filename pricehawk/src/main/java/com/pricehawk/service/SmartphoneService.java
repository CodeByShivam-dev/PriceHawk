package com.pricehawk.service;

import com.pricehawk.dto.SmartphonePriceResult;
import com.pricehawk.entity.PriceSnapshot;
import com.pricehawk.entity.SearchHistory;
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
 * Service responsible for fetching smartphone pricing data across multiple vendors.
 *
 * Key Features / Recruiter Highlights:
 * - Caching: Returns results from DB if captured within last 3 hours
 * - Parallel scraping: Amazon & Flipkart (Croma optional/future)
 * - Async specs API integration
 * - Fallback mechanism for demo/testing
 * - Async snapshot/history persistence
 * - Price sorting & specs injection for best-price card
 */
@Service
public class SmartphoneService
{

    private static final Logger log = LoggerFactory.getLogger(SmartphoneService.class);

    private final Executor apiExecutor;
    private final PriceScraperService scraperService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final PhoneSpecsService phoneSpecsService;

    // Demo-mode flag: if true, show placeholder cards when live results are empty
    private static final boolean DEMO_FALLBACK_ENABLED = true;

    public SmartphoneService(
            @Qualifier("apiExecutor") Executor apiExecutor,
            PriceScraperService scraperService,
            SearchHistoryRepository searchHistoryRepository,
            PriceSnapshotRepository priceSnapshotRepository,
            PhoneSpecsService phoneSpecsService
    )
    {
        this.apiExecutor = apiExecutor;
        this.scraperService = scraperService;
        this.searchHistoryRepository = searchHistoryRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.phoneSpecsService = phoneSpecsService;
    }

    /**
     * Fetches smartphone data for a query.
     *
     * Flow:
     * 1. Check DB cache first
     * 2. Scrape live vendors in parallel
     * 3. Fetch specs summary asynchronously
     * 4. Fallback to search URLs if no live data
     * 5. Inject specs into best-priced result
     * 6. Persist snapshots and search history asynchronously
     */

    public List<SmartphonePriceResult> fetchSmartphoneData(String query)
    {
        Instant now = Instant.now();
        Instant recentThreshold = now.minusSeconds(3 * 3600); // 3-hour caching window
        String normalized = query.toLowerCase().trim();

        // Fetch cached DB results ---
        List<PriceSnapshot> cached = priceSnapshotRepository
                .findByModelNormalizedAndCapturedAtAfter(normalized, recentThreshold);

        if (cached != null && !cached.isEmpty())
        {
            log.info("Serving cached DB results for query {}", query);
            return cached.stream()
                    .map(snap -> new SmartphonePriceResult(
                            snap.getStore(),
                            snap.getPrice(),
                            snap.getProductUrl(),
                            snap.getTitle(),
                            Boolean.TRUE.equals(snap.getInStock()),
                            snap.getImageUrl(),
                            snap.getRating(),
                            null // specsSummary not stored yet
                    ))
                    .collect(Collectors.toList());
        }

        //  Parallel scraping across vendors + specs ---
        CompletableFuture<SmartphonePriceResult> fAmazon = CompletableFuture.supplyAsync(
                () -> scraperService.scrapeAmazon(query).orElse(null), apiExecutor);

        CompletableFuture<SmartphonePriceResult> fFlipkart = CompletableFuture.supplyAsync(
                () -> scraperService.scrapeFlipkart(query).orElse(null), apiExecutor);

        // Specs async (external phone-specs API)
        CompletableFuture<String> fSpecs = CompletableFuture.supplyAsync(
                () -> phoneSpecsService.fetchSpecsSummary(query).orElse(null),
                apiExecutor
        );

        // Croma disabled for now
        // CompletableFuture<SmartphonePriceResult> fCroma = ...

        // Wait for all futures, but do not fail whole process if one fails
        CompletableFuture<Void> all = CompletableFuture.allOf(fAmazon, fFlipkart, fSpecs);

        try
        {
            all.get(15, TimeUnit.SECONDS);
        }
        catch (Exception ex)
        {
            log.warn("Partial scrape failed: {}", ex.getMessage());
        }

        // -Collect non-null results ---
        List<SmartphonePriceResult> rawResults = Arrays.asList(fAmazon, fFlipkart).stream()
                .map(f -> f.getNow(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Scraping done for '{}', results={}", query, rawResults.size());

        // Specs future result
        String specsSummary = fSpecs.getNow(null);

        //  Fallback - direct search links + placeholder image ---
        if (rawResults.isEmpty() && DEMO_FALLBACK_ENABLED)
        {
            log.warn("No live results for '{}'. Using direct search fallback cards.", query);
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

            rawResults = List.of(
                    new SmartphonePriceResult(
                            "Flipkart",
                            null,
                            "https://www.flipkart.com/search?q=" + encoded,
                            "Open Flipkart search for " + query,
                            true,
                            "https://placehold.co/300x300?text=Flipkart+Phone",
                            null,
                            null
                    ),
                    new SmartphonePriceResult(
                            "Amazon",
                            null,
                            "https://www.amazon.in/s?k=" + encoded,
                            "Open Amazon search for " + query,
                            true,
                            "https://placehold.co/300x300?text=Amazon+Phone",
                            null,
                            null
                    )
            );
        }

        // --- Step 3.6: Inject specs into best priced result (only when we have real prices) ---
        if (!rawResults.isEmpty() && specsSummary != null && !specsSummary.isBlank())
        {
            rawResults.stream()
                    .filter(r -> r.getPrice() != null)
                    .min(Comparator.comparingDouble(SmartphonePriceResult::getPrice))
                    .ifPresent(best -> best.setSpecsSummary(specsSummary));
        }

        //  Save snapshots and search history asynchronously (only for real results) ---
        List<SmartphonePriceResult> finalResults = rawResults; // effectively final for lambda

        if (!finalResults.isEmpty()
                && !(DEMO_FALLBACK_ENABLED && isDemoOnlyResults(finalResults))) {

            CompletableFuture.runAsync(() -> saveSnapshots(query, finalResults));
            CompletableFuture.runAsync(() -> saveHistory(query, finalResults.size()));
        }

        //: Return list, real prices first, then fallbacks ---
        return finalResults.stream()
                .sorted(Comparator.comparing(
                        SmartphonePriceResult::getPrice,
                        Comparator.nullsLast(Double::compareTo)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Heuristic: returns true if all results are demo fallback cards.
     * Prevents saving placeholder results to DB.
     */
    private boolean isDemoOnlyResults(List<SmartphonePriceResult> results)
    {
        return results.stream()
                .allMatch(r -> r.getTitle() != null && r.getTitle().contains("(demo fallback)"));
    }

    /**
     * Saves a snapshot of all fetched smartphone prices.
     * Each snapshot includes store, price, URL, title, image, rating, and timestamp.
     */

    private void saveSnapshots(String query, List<SmartphonePriceResult> results)
    {
        try
        {
            for (SmartphonePriceResult r : results)
            {
                PriceSnapshot snap = new PriceSnapshot(
                        query,
                        r.getStore(),
                        r.getPrice(),
                        r.getProductUrl(),
                        r.getImageUrl(),
                        r.getTitle(),
                        r.getRating(),
                        r.isInStock(),
                        null // Future: associate with userId if tracked
                );
                snap.setCapturedAt(Instant.now());
                priceSnapshotRepository.save(snap);
            }
        }
        catch (Exception e)
        {
            log.error("Snapshot save failed for {}", query, e);
        }
    }
    /**
     * Saves search history for analytics and monitoring.
     */
    private void saveHistory(String query, int count)
    {
        try
        {
            SearchHistory history = new SearchHistory(query, count, null);
            searchHistoryRepository.save(history);
        }
        catch (Exception e)
        {
            log.error("History save failed for {}", query, e);
        }
    }
}
