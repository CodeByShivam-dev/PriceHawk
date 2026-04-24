package com.pricehawk.scraper;

import com.pricehawk.dto.SmartphonePriceResult;
import java.util.List;

/**
 * Abstraction for a single source price fetcher.
 *
 * Each implementation is responsible for handling one vendor
 * (Amazon, Flipkart, Croma, etc.) and returning normalized results.
 *
 * This interface intentionally hides:
 * - scraping strategy (HTML parsing vs Selenium)
 * - retry / throttling logic
 * - anti-bot handling
 *
 * so the rest of the system can treat all vendors uniformly.
 */
public interface PriceFetcher extends AutoCloseable
{

    /**
     * Executes a search against the underlying source.
     *
     * Expectations from implementations:
     * - never throw on partial failures (return empty list instead)
     * - internally handle retries / transient failures
     * - return only usable results (skip broken cards / null prices)
     *
     * @param query raw user input (e.g. "iPhone 15 128GB")
     * @return list of normalized results; empty if nothing reliable found
     */
    List<SmartphonePriceResult> fetchPrices(String query);

    /**
     * Optional cleanup hook.
     *
     * Relevant for implementations that hold external resources
     * (e.g. Selenium WebDriver sessions).
     *
     * Jsoup-only implementations can safely ignore this.
     */
    @Override
    default void close()
    {
        // intentionally left blank
    }
}