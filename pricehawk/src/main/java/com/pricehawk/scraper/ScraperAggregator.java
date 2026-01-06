package com.pricehawk.scraper;

import com.pricehawk.dto.SmartphonePriceResult;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class ScraperAggregator
{
    // Each fetcher targets a different marketplace.
    // Kept as a list to allow easy enable/disable or reordering.
    private final List<PriceFetcher> fetchers;

    // Separate executor to avoid blocking web threads during scraping
    private final ExecutorService executor;

    public ScraperAggregator()
    {

        this.fetchers = List.of(
                new RobustFlipkartPriceFetcher(),
                new AmazonPriceFetcher(),
                new CromaPriceFetcher()
        );
        // Thread count tied to number of fetchers to keep things predictable
        this.executor = Executors.newFixedThreadPool(fetchers.size());
    }

    public List<SmartphonePriceResult> fetchAll(String query)
    {
        List<CompletableFuture<List<SmartphonePriceResult>>> futures = new ArrayList<>();

        for (PriceFetcher fetcher : fetchers)
        {
            CompletableFuture<List<SmartphonePriceResult>> future =
                    CompletableFuture.supplyAsync(() ->
                    {
                        try
                        {
                            List<SmartphonePriceResult> list = fetcher.fetchPrices(query);
                            log.info("Fetcher {} returned {} results for query='{}'",
                                    fetcher.getClass().getSimpleName(), list.size(), query);
                            return list;
                        }
                        catch (Exception e)
                        {
                            // Scraper failures are isolated by design
                            log.error("Fetcher error in {} for query '{}': {}",
                                    fetcher.getClass().getSimpleName(), query, e.getMessage());
                            return List.of();
                        }
                    }, executor);

            futures.add(future);
        }

        List<SmartphonePriceResult> combinedResults = new ArrayList<>();
        try
        {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<List<SmartphonePriceResult>> future : futures)
            {
                combinedResults.addAll(future.get());
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            log.error("Error combining scraper results for query '{}': {}", query, e.getMessage());
            Thread.currentThread().interrupt();
        }

        // If all scrapers fail, still return something usable for the UI
        if (combinedResults.isEmpty())
        {
            log.warn("All scrapers returned 0 results for query='{}'. Using direct search fallback.", query);

            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

            combinedResults = List.of(
                    new SmartphonePriceResult(
                            "Flipkart",
                            null, // live price unavailable
                            "https://www.flipkart.com/search?q=" + encoded,
                            "Open Flipkart search for " + query,
                            true,
                            "https://fpoimg.com/300x300?text=Flipkart+Phone", // placeholder image
                            null,
                            null
                    ),
                    new SmartphonePriceResult(
                            "Amazon",
                            null,
                            "https://www.amazon.in/s?k=" + encoded,
                            "Open Amazon search for " + query,
                            true,
                            "https://fpoimg.com/300x300?text=Amazon+Phone",
                            null,
                            null
                    )
            );
        }

        return combinedResults;
    }
    // Explicit shutdown to avoid thread leaks in long-running environments
    public void shutdown()
    {
        executor.shutdown();
    }
}
