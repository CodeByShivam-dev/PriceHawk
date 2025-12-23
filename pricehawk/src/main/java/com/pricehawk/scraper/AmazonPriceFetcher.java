package com.pricehawk.scraper;

import com.pricehawk.dto.SmartphonePriceResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
/**
 * Amazon-specific price fetcher implementation.
 *
 * Responsibility:
 * - Perform lightweight scraping of Amazon search results
 * - Extract best available product card data
 * - Convert raw HTML into structured domain DTOs
 *
 * This class intentionally focuses only on extraction logic.
 * Caching, persistence, retries, and orchestration are handled
 * at higher service layers.
 */

@Slf4j
public class AmazonPriceFetcher implements PriceFetcher
{
    /**
     * Custom user-agent to reduce the chance of bot blocking.
     * Mimics a modern desktop browser.
     */
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Fetch price results for a given search query from Amazon.
     *
     * Design choices:
     * - Returns a list to stay consistent with other fetchers,
     *   even though we currently extract only the top result.
     * - Fails gracefully: any missing critical field results in
     *   an empty list rather than throwing errors upstream.
     */

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query)
    {
        List<SmartphonePriceResult> results = new ArrayList<>();
        try
        {
            String url = "https://www.amazon.in/s?k=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .referrer("https://google.com")
                    .get();

            // Pick the first valid search result card
            Element card = doc.selectFirst("div.s-main-slot div[data-component-type='s-search-result']");
            if (card == null) return results;

            // Resolve product link (relative → absolute)
            Element linkElem = card.selectFirst("a.a-link-normal.s-no-outline");
            if (linkElem == null) return results;
            String href = linkElem.attr("href");
            String fullLink = href.startsWith("http") ? href : "https://www.amazon.in" + href;

            // Price is treated as mandatory; skip result if unavailable
            Element priceElem = card.selectFirst("span.a-price span.a-offscreen");
            if (priceElem == null) return results;
            Double price = parse(priceElem.text());
            if (price == null) return results;

            Element titleElem = card.selectFirst("span.a-size-medium, span.a-size-base-plus");
            String title = titleElem != null ? titleElem.text() : query;

            Double rating = null;
            Element ratingElem = card.selectFirst("span.a-icon-alt");
            if (ratingElem != null) {
                try { rating = Double.parseDouble(ratingElem.text().replaceAll("[^0-9.]", "")); }
                catch (Exception ignored) {}
            }

            boolean inStock = true;

            results.add(new SmartphonePriceResult(
                    "Amazon", price, fullLink, title, inStock,
                    "https://via.placeholder.com/300.png?text=Amazon+Phone", rating, null
            ));

        } catch (Exception e) {
            log.error("Amazon fetch error for query '{}': {}", query, e.getMessage());
        }

        return results;
    }

    /**
     * Utility to normalize price strings into numeric values.
     * Handles currency symbols and thousand separators.
     */

    private Double parse(String p) {
        try {
            String cleaned = p.replaceAll("[^0-9.,]", "").replace(",", "");
            if (cleaned.isBlank()) return null;
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return null;
        }
    }
}
