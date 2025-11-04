package com.pricehawk.service;

import com.pricehawk.dto.SmartphoneDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * 🧠 SmartphoneService
 *
 * This service handles the **core business logic** of the PriceHawk system.
 * It runs 3 fake API calls (Amazon, Flipkart, Croma) in parallel
 * to simulate a real-world price comparison between multiple stores.
 *
 * ✅ Multithreading powered by ThreadPoolExecutor (configured in AsyncConfig)
 * ✅ Returns clean DTO-based data for controller
 * ✅ Real APIs (Amazon, Flipkart, etc.) can be easily added later
 */

@Service
public class SmartphoneService {

    private final Executor apiExecutor;

    // Constructor-based injection (recommended over @Autowired for Services)
    public SmartphoneService(@Qualifier("apiExecutor") Executor apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * 🚀 Fetch smartphone data in parallel (simulated async API calls)
     *
     * @param query — User’s search term (e.g. "iPhone 15")
     * @return List of SmartphoneDTO (price comparison results)
     */
    public List<SmartphoneDTO> fetchSmartphoneData(String query) {

        // 📋 Store tasks for each e-commerce site
        List<Callable<SmartphoneDTO>> tasks = List.of(
                () -> mockFetch("Amazon", 74999, 4.5, true),
                () -> mockFetch("Flipkart", 73999, 4.6, true),
                () -> mockFetch("Croma", 75999, 4.4, false)
        );

        // ✅ Run all tasks in parallel using our async executor
        List<SmartphoneDTO> results = new ArrayList<>();
        try {
            List<Future<SmartphoneDTO>> futures = ((ExecutorService) apiExecutor).invokeAll(tasks);

            // 🧾 Collect results from all APIs
            for (Future<SmartphoneDTO> future : futures) {
                results.add(future.get()); // waits for each task to finish
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * 🎭 Mock API Simulation
     * This mimics calling an external API and returning data.
     * In future, replace with real HTTP API call using RestTemplate/WebClient.
     */
    private SmartphoneDTO mockFetch(String store, double price, double rating, boolean inStock) throws InterruptedException {
        Thread.sleep(800); // Simulate API latency
        return new SmartphoneDTO(store, price, rating, inStock);
    }
}
