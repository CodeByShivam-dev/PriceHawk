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
 */
@Service
public class SmartphoneService {

    private static final Logger log = LoggerFactory.getLogger(SmartphoneService.class);

    private final Executor apiExecutor;
    private final PriceScraperService scraperService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;

    // Demo-mode flag: true => empty result pe fallback demo cards
    private static final boolean DEMO_FALLBACK_ENABLED = true;

    public SmartphoneService(
            @Qualifier("apiExecutor") Executor apiExecutor,
            PriceScraperService scraperService,
            SearchHistoryRepository searchHistoryRepository,
            PriceSnapshotRepository priceSnapshotRepository
    ) {
        this.apiExecutor = apiExecutor;
        this.scraperService = scraperService;
        this.searchHistoryRepository = searchHistoryRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    public List<SmartphonePriceResult> fetchSmartphoneData(String query) {
        Instant now = Instant.now();
        Instant recentThreshold = now.minusSeconds(3 * 3600); // 3-hour caching window
        String normalized = query.toLowerCase().trim();

        // --- Step 1: Fetch cached DB results ---
        List<PriceSnapshot> cached = priceSnapshotRepository
                .findByModelNormalizedAndCapturedAtAfter(normalized, recentThreshold);

        if (cached != null && !cached.isEmpty()) {
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

        // Croma disabled for now
        // CompletableFuture<SmartphonePriceResult> fCroma = ...

        CompletableFuture<Void> all = CompletableFuture.allOf(fAmazon, fFlipkart);

        try {
            all.get(15, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("Partial scrape failed: {}", ex.getMessage());
        }

        // --- Step 3: Collect non-null results ---
        List<SmartphonePriceResult> rawResults = Arrays.asList(fAmazon, fFlipkart).stream()
                .map(f -> f.getNow(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Scraping done for '{}', results={}", query, rawResults.size());

        // --- Step 3.5: Demo fallback if everything failed ---
        if (rawResults.isEmpty() && DEMO_FALLBACK_ENABLED) {
            log.warn("No live results for '{}'. Using demo fallback cards.", query);
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

            rawResults = List.of(
                    new SmartphonePriceResult(
                            "Flipkart",
                            74999.0,
                            "https://www.flipkart.com/search?q=" + encoded,
                            query + " (demo fallback)",
                            true,
                            null,
                            4.5
                    ),
                    new SmartphonePriceResult(
                            "Amazon",
                            75999.0,
                            "https://www.amazon.in/s?k=" + encoded,
                            query + " (demo fallback)",
                            true,
                            null,
                            4.3
                    )
            );
        }

        // --- Step 4: Save snapshots and search history asynchronously (only for real results) ---
        List<SmartphonePriceResult> finalResults = rawResults; // effectively final for lambda

        if (!finalResults.isEmpty()
                && !(DEMO_FALLBACK_ENABLED && isDemoOnlyResults(finalResults))) {

            CompletableFuture.runAsync(() -> saveSnapshots(query, finalResults));
            CompletableFuture.runAsync(() -> saveHistory(query, finalResults.size()));
        }

        // --- Step 5: Return sorted list with valid prices ---
        return finalResults.stream()
                .filter(r -> r.getPrice() != null)
                .sorted(Comparator.comparingDouble(SmartphonePriceResult::getPrice))
                .collect(Collectors.toList());
    }

    // Heuristic: agar sabhi titles me "(demo fallback)" ho, to unko DB me log na karo
    private boolean isDemoOnlyResults(List<SmartphonePriceResult> results) {
        return results.stream()
                .allMatch(r -> r.getTitle() != null && r.getTitle().contains("(demo fallback)"));
    }

    private void saveSnapshots(String query, List<SmartphonePriceResult> results) {
        try {
            for (SmartphonePriceResult r : results) {
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
        } catch (Exception e) {
            log.error("Snapshot save failed for {}", query, e);
        }
    }

    private void saveHistory(String query, int count) {
        try {
            SearchHistory history = new SearchHistory(query, count, null);
            searchHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("History save failed for {}", query, e);
        }
    }
}
