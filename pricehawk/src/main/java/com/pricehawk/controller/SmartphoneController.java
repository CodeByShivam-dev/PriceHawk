package com.pricehawk.controller;

import com.pricehawk.dto.SmartphonePriceResult;
import com.pricehawk.exception.InvalidQueryException;
import com.pricehawk.service.SmartphoneService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/smartphones")
@Slf4j
public class SmartphoneController
{

    private final SmartphoneService smartphoneService;

    @Autowired
    public SmartphoneController(SmartphoneService smartphoneService)
    {
        this.smartphoneService = smartphoneService;
    }

    @GetMapping
    public ResponseEntity<List<SmartphonePriceResult>> searchSmartphones(
            @RequestParam(name = "query") String query)
    {

        log.info("Incoming search request: query='{}' at={}", query, Instant.now());

        if (!StringUtils.hasText(query))
        {
            log.warn("Invalid search query received (empty/null).");
            throw new InvalidQueryException("query parameter is required and cannot be empty");
        }

        // Fetch data
        final List<SmartphonePriceResult> results = smartphoneService.fetchSmartphoneData(query);

        // Convert (lambda-safe)
        List<SmartphonePriceResult> dtos = results.stream()
                .map(r -> new SmartphonePriceResult(
                        r.getStore(),
                        r.getPrice(),
                        r.getProductUrl(),
                        r.getTitle(),
                        r.isInStock()
                ))
                .collect(Collectors.toList());

        log.info("Search completed for '{}', results={}", query, dtos.size());

        return ResponseEntity.ok(dtos);
    }
}
