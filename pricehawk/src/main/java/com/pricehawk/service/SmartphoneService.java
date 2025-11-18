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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * SmartphoneService — updated to use real scrapers via PriceScraperService
 */
@Service
public class SmartphoneService {

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
    ) {
        this.apiExecutor = apiExecutor;
        this.scraperService = scraperService;
        this.searchHistoryRepository = searchHistoryRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    /**
     * Fetch live prices from Amazon, Flipkart, Croma in parallel.
     * Return list of successful SmartphonePriceResult.
     */
    public List<SmartphonePriceResult> fetchSmartphoneData(String query) {

        // Prepare futures (wrap Optional result into nullable result)
        CompletableFuture<SmartphonePriceResult> fAmazon = CompletableFuture.supplyAsync(() ->
                scraperService.scrapeAmazon(query).orElse(null), apiExecutor);

        CompletableFuture<SmartphonePriceResult> fFlipkart = CompletableFuture.supplyAsync(() ->
                scraperService.scrapeFlipkart(query).orElse(null), apiExecutor);

        CompletableFuture<SmartphonePriceResult> fCroma = CompletableFuture.supplyAsync(() ->
                scraperService.scrapeCroma(query).orElse(null), apiExecutor);

        // Wait for all, but don't fail fast — gather successful ones
        CompletableFuture<Void> all = CompletableFuture.allOf(fAmazon, fFlipkart, fCroma);

        try {
            // Wait (bounded) for completion
            all.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while scraping for query={}", query);
        } catch (ExecutionException | TimeoutException ex) {
            log.warn("Timeout or execution error while scraping for query={}, continuing with what we have", query, ex);
        }

        List<SmartphonePriceResult> rawResults = Arrays.asList(fAmazon, fFlipkart, fCroma).stream()
                .map(f -> {
                    try {
                        return f.getNow(null); // if not completed, returns null
                    } catch (CompletionException ce) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Scraping complete for query='{}'. successful results={}", query, rawResults.size());

        // Persist price snapshots for successful results (non-blocking)
        CompletableFuture.runAsync(() -> {
            try {
                for (SmartphonePriceResult r : rawResults) {
                    PriceSnapshot snap = new PriceSnapshot(query, r.getStore(), r.getPrice(), r.getProductUrl());
                    snap.setCapturedAt(LocalDateTime.now());
                    priceSnapshotRepository.save(snap);
                }
            } catch (Exception e) {
                log.error("Failed to save price snapshots for query={}", query, e);
            }
        });

        // Save search history async
        CompletableFuture.runAsync(() -> {
            try {
                SearchHistory history = new SearchHistory(query, rawResults.size());
                searchHistoryRepository.save(history);
            } catch (Exception e) {
                log.error("Failed to save search history for query={}", query, e);
            }
        });

        // If no results, return empty list (controller should handle)
        if (rawResults.isEmpty()) {
            log.warn("No scraping results available for query={}", query);
            return Collections.emptyList();
        }

        // Return results sorted by price ascending (cheapest first)
        List<SmartphonePriceResult> sorted = rawResults.stream()
                .filter(r -> r.getPrice() != null)
                .sorted(Comparator.comparingDouble(SmartphonePriceResult::getPrice))
                .collect(Collectors.toList());

        return sorted;
    }

    /**
     * Simple verdict generator (can be upgraded to AI-based later).
     * Example: "Flipkart offers best deal at ₹73,999 (₹2,000 cheaper than Amazon)"
     */
    public String generateVerdict(List<SmartphonePriceResult> results) {
        if (results == null || results.isEmpty()) return "No results to judge.";

        SmartphonePriceResult best = results.get(0);
        Optional<SmartphonePriceResult> next = results.stream().skip(1).findFirst();

        String base = String.format("%s offers the best deal at ₹%.0f", best.getStore(), best.getPrice());
        if (next.isPresent()) {
            double diff = next.get().getPrice() - best.getPrice();
            if (diff > 0) {
                base += String.format(" (₹%.0f cheaper than %s)", diff, next.get().getStore());
            }
        }
        return base;
    }
}
