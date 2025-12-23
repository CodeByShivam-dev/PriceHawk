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
 * Croma-specific price fetcher.
 *
 * Purpose:
 * - Extract top smartphone listing details from Croma search results
 * - Keep scraping logic resilient against frequent frontend layout changes
 *
 * This fetcher is intentionally defensive:
 * missing or malformed fields result in graceful fallback,
 * not system-wide failures.
 */
@Slf4j
public class CromaPriceFetcher implements PriceFetcher
{
    /**
     * Desktop browser user-agent to minimize bot detection.
     */
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Fetches price data for a given query from Croma.
     *
     * Design notes:
     * - Returns a list for interface consistency across fetchers
     * - Extracts only the most relevant/top product to reduce noise
     * - Allows partial platform failures without breaking aggregation
     */

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query)
    {
        List<SmartphonePriceResult> results = new ArrayList<>();

        try
        {
            String url = "https://www.croma.com/search/?text=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .referrer("https://google.com")
                    .get();

            // Attempt primary selectors first; fallback added for layout variations
            Element card = doc.selectFirst("li.product-item, div.product-item, div.product__list--item");
            if (card == null)
            {

                card = doc.selectFirst("div.product-block, div.js-product-tile");
            }
            if (card == null)
            {
                log.warn("Croma: no product card found for query='{}'", query);
                return results;
            }

            // Resolve product link (relative URLs are common on Croma)
            Element linkEl = card.selectFirst("a.product-title, a.js-product-url, a");
            if (linkEl == null)
            {
                log.warn("Croma: product link not found for query='{}'", query);
                return results;
            }
            String href = linkEl.attr("href");
            if (href == null || href.isBlank())
            {
                log.warn("Croma: href missing for query='{}'", query);
                return results;
            }
            String fullLink = href.startsWith("http")
                    ? href
                    : "https://www.croma.com" + href;

            // Price
            Element priceEl = card.selectFirst("span.amount, span.new-price, span.pay-price");
            if (priceEl == null)
            {
                log.warn("Croma: price element not found for query='{}'", query);
                return results;
            }
            String priceText = priceEl.text();
            Double price = parse(priceText);
            if (price == null)
            {
                log.warn("Croma: could not parse price '{}' for query='{}'", priceText, query);
                return results;
            }

            // Title
            String title = linkEl.text();
            if (title == null || title.isBlank())
            {
                title = query;
            }

            // Rating optional (Croma often not show directly in listing)
            Double rating = null;

            boolean inStock = true; // listing me assume true

            // Naya DTO: store, price, url, title, inStock, imageUrl, rating
            results.add(
                    new SmartphonePriceResult(
                            "Croma",
                            price,
                            fullLink,
                            title,
                            inStock,
                            null,     // imageUrl
                            rating,
                            null      // specsSummary
                    )
            );

        }
        catch (Exception e)
        {
            log.error("Croma fetch error for query '{}': {}", query, e.getMessage());
        }

        return results;
    }
    /**
     * Normalizes raw price strings into numeric values.
     * Handles currency symbols and formatting inconsistencies.
     */
    private Double parse(String p)
    {
        try
        {
            String cleaned = p.replaceAll("[^0-9.,]", "")
                    .replace(",", "")
                    .trim();
            if (cleaned.isBlank()) return null;
            return Double.parseDouble(cleaned);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
