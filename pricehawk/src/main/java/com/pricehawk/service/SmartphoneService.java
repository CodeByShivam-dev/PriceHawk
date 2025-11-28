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

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service responsible for fetching smartphone pricing data across multiple vendors.
 *
 * Design Intent:
 * - Provide near real-time prices while leveraging DB caching to reduce scraper load.
 * - Execute parallel scraping for performance (Amazon, Flipkart, Croma) using Executor.
 * - Maintain search history and snapshots asynchronously for analytics and future tracking.
 *
 * Notes for future enhancements:
 * - Support user-specific tracking (userId)
 * - Retry/fallback strategies for partial failures
 * - Rate-limiting or throttling to avoid anti-bot detection
 */
@Service
public class SmartphoneService
{

    private static final Logger log = LoggerFactory.getLogger(SmartphoneService.class);

    private final Executor apiExecutor;
    private final PriceScraperService scraperService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;

    public SmartphoneService(
            @Qualifier("apiExecutor") Executor apiExecutor,
            PriceScraperService scraperService,
            SearchHistoryRepository searchHistoryRepository,
            PriceSnapshotRepository priceSnapshotRepository
    )
    {
        this.apiExecutor = apiExecutor;
        this.scraperService = scraperService;
        this.searchHistoryRepository = searchHistoryRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    /**
     * Fetch smartphone pricing data for a given query.
     *
     * Workflow:
     * 1️⃣ Check DB cache for recent snapshots (within 3 hours) → reduces scraping load.
     * 2️⃣ If cache is empty or stale, execute parallel scraping across vendors.
     * 3️⃣ Collect partial results even if some scrapers fail.
     * 4️⃣ Save snapshots and search history asynchronously.
     * 5️⃣ Return price-sorted list with non-null prices.
     *
     * @param query User search query (model name)
     * @return List of smartphone pricing results sorted by price.
     */
    public List<SmartphonePriceResult> fetchSmartphoneData(String query)
    {
        Instant now = Instant.now();
        Instant recentThreshold = now.minusSeconds(3 * 3600); // 3-hour caching window

        // --- Step 1: Fetch cached DB results ---
        List<PriceSnapshot> cached = priceSnapshotRepository
                .findByModelNormalizedAndCapturedAtAfter(query.toLowerCase().trim(), recentThreshold);

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
                            snap.getRating()
                    ))
                    .collect(Collectors.toList());
        }

        // --- Step 2: Parallel scraping across vendors ---
        CompletableFuture<SmartphonePriceResult> fAmazon = CompletableFuture.supplyAsync(
                () -> scraperService.scrapeAmazon(query).orElse(null), apiExecutor);
        CompletableFuture<SmartphonePriceResult> fFlipkart = CompletableFuture.supplyAsync(
                () -> scraperService.scrapeFlipkart(query).orElse(null), apiExecutor);
        CompletableFuture<SmartphonePriceResult> fCroma = CompletableFuture.supplyAsync(
                () -> scraperService.scrapeCroma(query).orElse(null), apiExecutor);

        CompletableFuture<Void> all = CompletableFuture.allOf(fAmazon, fFlipkart, fCroma);

        try
        {
            // Timeout ensures system doesn't block indefinitely if a vendor is slow
            all.get(15, TimeUnit.SECONDS);
        }
        catch (Exception ex)
        {
            log.warn("Partial scrape failed: {}", ex.getMessage());
        }

        // --- Step 3: Collect non-null results ---
        List<SmartphonePriceResult> rawResults = Arrays.asList(fAmazon, fFlipkart, fCroma).stream()
                .map(f -> f.getNow(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Scraping done for '{}', results={}", query, rawResults.size());

        // --- Step 4: Save snapshots and search history asynchronously ---
        CompletableFuture.runAsync(() -> saveSnapshots(query, rawResults));
        CompletableFuture.runAsync(() -> saveHistory(query, rawResults.size()));

        // --- Step 5: Return sorted list with valid prices ---
        return rawResults.stream()
                .filter(r -> r.getPrice() != null)
                .sorted(Comparator.comparingDouble(SmartphonePriceResult::getPrice))
                .collect(Collectors.toList());
    }

    /**
     * Persist price snapshots for analytics and caching.
     *
     * Note:
     * - Currently no userId association; can be extended for personalized tracking.
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
     * Record search query history for analytics and metrics.
     *
     * - Helps understand trending models
     * - Can be used for recommendation engine in future
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
