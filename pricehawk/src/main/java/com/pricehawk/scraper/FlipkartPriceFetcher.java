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
import java.util.stream.Collectors;
/**
 * Fetches smartphone pricing data from Flipkart search listings.
 *
 * Design considerations:
 * - Uses Jsoup HTML scraping (no official Flipkart API)
 * - Defensive selectors to handle UI changes
 * - Extracts only first relevant product for speed & stability
 * - Gracefully fails with empty result (never throws upstream)
 */
@Slf4j
public class FlipkartPriceFetcher implements PriceFetcher
{
    /**
     * Desktop browser User-Agent to avoid bot blocking / mobile layouts.
     */
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Scrapes Flipkart search results for the given query.
     *
     * @param query smartphone model entered by user
     * @return list containing a single best-match product (or empty if not found)
     */

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query)
    {
        List<SmartphonePriceResult> results = new ArrayList<>();

        try
        {
            // Encode query to safely handle spaces and special characters
            String url = "https://www.flipkart.com/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

// Fetch HTML document with timeout & referrer for better reliability
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .referrer("https://google.com")
                    .get();


            // Primary product card selector (Flipkart changes class names frequently)
            Element card = doc.selectFirst("div._2kHMtA");
            if (card == null) card = doc.selectFirst("div._4ddWXP, div._1AtVbE");
            if (card == null) return results;

            Element linkEl = card.selectFirst("a._1fQZEK, a.s1Q9rs, a._2rpwqI");
            if (linkEl == null) return results;
            String href = linkEl.attr("href");
            if (href == null || href.isBlank()) return results;
            String fullLink = href.startsWith("http") ? href : "https://www.flipkart.com" + href;

            Element priceEl = card.selectFirst("div._30jeq3");
            if (priceEl == null) return results;
            Double price = parse(priceEl.text());
            if (price == null) return results;

            // Title resolution (attribute preferred over visible text)
            String title = linkEl.attr("title");
            if (title == null || title.isBlank()) title = linkEl.text();
            if (title == null || title.isBlank()) title = query;

            // Optional specs summary from listing bullet points
            String specsSummary = null;
            Element specList = card.selectFirst("ul._1xgFaf");
            if (specList != null)
            {
                specsSummary = specList.select("li").stream().limit(4)
                        .map(Element::text).collect(Collectors.joining(" · "));
            }

            boolean inStock = true;

            results.add(new SmartphonePriceResult(
                    "Flipkart", price, fullLink, title, inStock,
                    "https://via.placeholder.com/300.png?text=Flipkart+Phone", null, specsSummary
            ));

        }
        catch (Exception e)
        {
            log.error("Flipkart fetch error for query '{}': {}", query, e.getMessage());
        }

        return results;
    }

    /**
     * Parses price string into Double.
     * Example: "₹12,999" → 12999.0
     */
    private Double parse(String p)
    {
        try
        {
            String cleaned = p.replaceAll("[^0-9.,]", "").replace(",", "").trim();
            if (cleaned.isBlank()) return null;
            return Double.parseDouble(cleaned);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
