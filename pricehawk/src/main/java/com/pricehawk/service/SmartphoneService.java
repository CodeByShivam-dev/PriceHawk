package com.pricehawk.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * ✅ Service Layer — Main logic for fetching and combining smartphone data
 * from multiple sources using multithreading.
 *
 * This layer handles business logic between Controller and external APIs.
 * (Currently, APIs are mocked with dummy data.)
 */
@Service
public class SmartphoneService
{

    // Executor used to run multiple tasks (threads) in parallel
    private final Executor executor;

    // Constructor injection — takes the thread pool bean from AsyncConfig
    public SmartphoneService(@Qualifier("apiExecutor") Executor executor)
    {
        this.executor = executor;
    }

    /**
     * 🧠 Main method to fetch smartphone data from 3 mock APIs:
     * Amazon, Flipkart, and Croma — all running at the same time.
     *
     * @param query - smartphone name entered by user (like "iPhone 15")
     * @return List of results sorted by price
     */
    public List<Map<String, Object>> fetchSmartphoneData(String query)
    {

        // Run all 3 API tasks asynchronously (in parallel)
        CompletableFuture<Map<String, Object>> amazonData =
                CompletableFuture.supplyAsync(() -> getFromAmazon(query), executor);

        CompletableFuture<Map<String, Object>> flipkartData =
                CompletableFuture.supplyAsync(() -> getFromFlipkart(query), executor);

        CompletableFuture<Map<String, Object>> cromaData =
                CompletableFuture.supplyAsync(() -> getFromCroma(query), executor);

        // Wait for all tasks to complete before moving ahead
        CompletableFuture.allOf(amazonData, flipkartData, cromaData).join();

        // Combine results from all APIs into one list
        List<Map<String, Object>> results = new ArrayList<>();
        try
        {
            results.add(amazonData.get());
            results.add(flipkartData.get());
            results.add(cromaData.get());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Sort all results by price (lowest to highest)
        results.sort(Comparator.comparing(r -> (Double) r.get("price")));

        // Return the final sorted list
        return results;
    }

    // ---------------- MOCK API CALLS ----------------
    // (In real project, we’ll replace these with actual REST API calls)

    /**
     * 🛒 Simulates fetching product data from Amazon API
     * Adds small delay to feel like a real network call
     */
    private Map<String, Object> getFromAmazon(String query)
    {
        delay(800); // waits for 800ms
        return Map.of(
                "store", "Amazon",
                "product", query,
                "price", 75999.0,
                "rating", 4.6
        );
    }

    /**
     * 🛒 Simulates fetching product data from Flipkart API
     */
    private Map<String, Object> getFromFlipkart(String query)
    {
        delay(1000); // waits for 1 second
        return Map.of(
                "store", "Flipkart",
                "product", query,
                "price", 74999.0,
                "rating", 4.5
        );
    }

    /**
     * 🛒 Simulates fetching product data from Croma API
     */
    private Map<String, Object> getFromCroma(String query)
    {
        delay(600); // waits for 0.6 second
        return Map.of(
                "store", "Croma",
                "product", query,
                "price", 75500.0,
                "rating", 4.4
        );
    }

    /**
     * 🕐 Helper method — pauses thread for a few milliseconds
     * to simulate real API call delay
     */
    private void delay(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }
}
