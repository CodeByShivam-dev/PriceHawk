package com.pricehawk.controller;

import com.pricehawk.dto.SmartphonePriceResult;
import com.pricehawk.exception.InvalidQueryException;
import com.pricehawk.service.SmartphoneService;
import com.pricehawk.service.PhoneApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.pricehawk.dto.PhoneDetailResponse;
import com.pricehawk.model.SearchResponse;

import java.time.Instant;
import java.util.List;

@RestController
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:8080",
        "https://price-hawk-ochre.vercel.app"
})
@RequestMapping("/api/smartphones")
@Slf4j
public class SmartphoneController
{

    private final SmartphoneService smartphoneService;
    private final PhoneApiService phoneApiService; // external specs provider (GSMArena)

    @Autowired
    public SmartphoneController(SmartphoneService smartphoneService,
                                PhoneApiService phoneApiService)
    {
        this.smartphoneService = smartphoneService;
        this.phoneApiService = phoneApiService;
    }

    /**
     * Primary search endpoint.
     *
     * Flow:
     * 1) Attempt multi-source scraping (Amazon / Flipkart etc.)
     * 2) If no usable results → fallback to GSMArena API
     * 3) Always return at least one meaningful response (never empty UX)
     */
    @GetMapping
    public ResponseEntity<List<SmartphonePriceResult>> searchSmartphones(
            @RequestParam(name = "query") String query)
    {

        log.info("Incoming search request: query='{}' at={}", query, Instant.now());

        // Basic input guard — avoids unnecessary downstream calls
        if (!StringUtils.hasText(query))
        {
            log.warn("Invalid search query received (empty/null).");
            throw new InvalidQueryException("query parameter is required and cannot be empty");
        }

        // Step 1: primary data source → scrapers
        List<SmartphonePriceResult> results =
                smartphoneService.fetchSmartphoneData(query);

        // Step 2: fallback → structured API (more stable but limited pricing data)
        if (results.isEmpty())
        {
            log.info("Scrapers returned no results, falling back to GSMArena API for '{}'", query);
            results = createGSMArenaFallback(phoneApiService, query);
        }

        log.info("Search completed for '{}', results={}", query, results.size());
        return ResponseEntity.ok(results);
    }

    /**
     * Fallback strategy when scrapers fail.
     *
     * Uses GSMArena API to at least provide:
     * - device name
     * - key specs summary
     * - image
     *

     */
    private List<SmartphonePriceResult> createGSMArenaFallback(PhoneApiService service, String query) {
        try
        {
            SearchResponse[] searchResults = service.searchPhone(query);

            if (searchResults != null && searchResults.length > 0)
            {
                String slug = searchResults[0].getSlug();

                PhoneDetailResponse details = service.getPhoneDetails(slug);

                SmartphonePriceResult result = SmartphonePriceResult.builder()
                        .store("📱 GSMArena")
                        .title(details.getName() != null ? details.getName() : query)
                        // condensed specs for UI display (not full payload)
                        .specsSummary(formatSpecsForAI(details))
                        .imageUrl(details.getImg() != null ? details.getImg() : "")
                        .price(0) // placeholder → indicates "price unavailable"
                        .build();

                return List.of(result);
            }

        }
        catch (Exception e)
        {
            // API failure should not break response contract
            log.warn("GSMArena API fallback failed: {}", e.getMessage());
        }

        // Final safety net → ensures UI always has something clickable
        return List.of(
                SmartphonePriceResult.builder()
                        .store("🔍 Flipkart Search")
                        .title(query)
                        .specsSummary("📱 Premium flagship · 🧠 Latest chipset · 📸 Pro camera · 🔋 All-day battery")
                        .build()
        );
    }

    /**
     * Builds a compact, emoji-friendly specs string for quick UI consumption.
     * Keeps payload small instead of sending full spec tree.
     */
    private String formatSpecsForAI(PhoneDetailResponse details)
    {
        StringBuilder specs = new StringBuilder();

        if (details.getDisplaySize() != null)
            specs.append("📱 ").append(details.getDisplaySize());

        if (details.getChipset() != null)
            specs.append(" · 🧠 ").append(details.getChipset());

        if (details.getBattery() != null)
            specs.append(" · 🔋 ").append(details.getBattery());

        return specs.length() > 0
                ? specs.toString()
                : "📱 Full specs available";
    }
}