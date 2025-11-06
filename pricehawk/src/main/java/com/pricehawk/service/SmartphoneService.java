package com.pricehawk.service;

import com.pricehawk.dto.SmartphoneDTO;
import com.pricehawk.entity.SearchHistory;
import com.pricehawk.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * 🧠 SmartphoneService
 *
 * Core business logic of PriceHawk system:
 * ✅ Compares prices from multiple fake stores (Amazon, Flipkart, Croma)
 * ✅ Runs all store calls concurrently using ThreadPoolExecutor
 * ✅ Logs each user search asynchronously into the database
 *
 * Future-ready:
 * - Replace mockFetch() with real API calls.
 * - Analyze SearchHistory table for trending phones.
 */

@Service
public class SmartphoneService {

    private final Executor apiExecutor;
    private final SearchHistoryRepository searchHistoryRepository;

    // Constructor-based Dependency Injection (best practice)
    public SmartphoneService(
            @Qualifier("apiExecutor") Executor apiExecutor,
            SearchHistoryRepository searchHistoryRepository
    ) {
        this.apiExecutor = apiExecutor;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    /**
     * 🚀 Fetch smartphone data in parallel (simulated async API calls)
     *
     * @param query — User’s search term (e.g. "iPhone 15")
     * @return List of SmartphoneDTO (price comparison results)
     */
    public List<SmartphoneDTO> fetchSmartphoneData(String query) {

        // 📋 Tasks representing each e-commerce API call
        List<Callable<SmartphoneDTO>> tasks = List.of(
                () -> mockFetch("Amazon", 74999, 4.5, true),
                () -> mockFetch("Flipkart", 73999, 4.6, true),
                () -> mockFetch("Croma", 75999, 4.4, false)
        );

        List<SmartphoneDTO> results = new ArrayList<>();

        try {
            // 🧵 Run all calls in parallel
            List<Future<SmartphoneDTO>> futures = ((ExecutorService) apiExecutor).invokeAll(tasks);

            // 🧾 Collect and add results
            for (Future<SmartphoneDTO> future : futures) {
                results.add(future.get());
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // 💾 Async logging (doesn’t block API response)
        CompletableFuture.runAsync(() -> {
            SearchHistory history = new SearchHistory(query, results.size());
            searchHistoryRepository.save(history);
        });

        return results;
    }

    /**
     * 🎭 Mock API Simulation
     * Simulates calling external APIs like Amazon, Flipkart, etc.
     */
    private SmartphoneDTO mockFetch(String store, double price, double rating, boolean inStock)
            throws InterruptedException {
        Thread.sleep(800); // simulate network delay
        return new SmartphoneDTO(store, price, rating, inStock);
    }
}
