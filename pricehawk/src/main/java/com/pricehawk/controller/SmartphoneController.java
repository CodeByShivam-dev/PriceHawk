package com.pricehawk.controller;

import com.pricehawk.dto.SmartphoneDTO;
import com.pricehawk.service.SmartphoneService;
import com.pricehawk.exception.InvalidQueryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * SmartphoneController
 *
 * GET  /api/smartphones?query={query}
 *
 * Example:
 *   GET /api/smartphones?query=iPhone+15
 *
 * Responsibilities:
 * - validate input
 * - log request metadata
 * - call service layer to fetch comparison results
 * - return JSON list of SmartphoneDTO
 */
@RestController
@RequestMapping("/api/smartphones")
@Slf4j
public class SmartphoneController {

    private final SmartphoneService smartphoneService;

    @Autowired
    public SmartphoneController(SmartphoneService smartphoneService) {
        this.smartphoneService = smartphoneService;
    }

    @GetMapping
    public ResponseEntity<List<SmartphoneDTO>> searchSmartphones(@RequestParam(name = "query") String query) {
        log.info("Incoming search request: query='{}' at={}", query, Instant.now());

        if (!StringUtils.hasText(query)) {
            log.warn("Invalid search query received (empty/null).");
            throw new InvalidQueryException("query parameter is required and cannot be empty");
        }

        // Delegate to service (which currently returns mock data; will be replaced with Jsoup scrapers)
        List<SmartphoneDTO> results = smartphoneService.fetchSmartphoneData(query);

        log.info("Search completed for query='{}'. resultsCount={}", query, results == null ? 0 : results.size());
        return ResponseEntity.ok(results);
    }
}
