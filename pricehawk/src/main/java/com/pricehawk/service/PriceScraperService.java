package com.pricehawk.service;

import com.pricehawk.dto.SmartphonePriceResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * PriceScraperService
 *
 * Central service responsible for extracting product, pricing,
 * availability, rating, and image details from multiple Indian ecommerce
 * platforms (Amazon, Flipkart, Croma).
 *
 * Design goals:
 *  - Fail-safe scraping → fallback selectors + retry logic
 *  - Vendor-independent structure → each site isolated
 *  - Return consistent DTO across vendors
 *  - Minimal assumptions about HTML stability (ecommerce sites update often)
 */
@Service
public class PriceScraperService
{

    private static final Logger log = LoggerFactory.getLogger(PriceScraperService.class);

    /**
     * A realistic browser UA string is mandatory to reduce bot detection,
     * especially on Amazon & Flipkart, which use aggressive anti-scraping systems.
     */
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private static final int TIMEOUT = 10000;

    /**
     * Attempt common selector sets for product images across different sellers.
     * This avoids site-specific duplication and helps automatically adapt when UI changes.
     */
    private String getImageUrl(Document doc)
    {
        for (String selector : new String[]
                {
                "img#landingImage",
                "img.s-image",
                "img._396cs4",
                "img.pdp-img"
        })
        {
            Element img = doc.selectFirst(selector);
            if (img != null) return img.absUrl("src");
        }
        return null;
    }

    /**
     * Multi-vendor rating extraction. Ratings use different markup depending
     * on category, layout, and whether the product is ad-boosted.
     */
    private Double getRating(Document doc)
    {
        for (String selector : new String[]
                {
                "span.a-icon-alt",
                "div._3LWZlK"
        })
        {
            Element r = doc.selectFirst(selector);
            if (r != null)
            {
                String txt = r.text().replaceAll("[^\\d.]", "");
                try
                {
                    return Double.parseDouble(txt);
                }
                catch (Exception ignore)
                {}
            }
        }
        return null;
    }

    /**
     * Wrapper around Jsoup GET with:
     *  - 1st attempt (normal)
     *  - 2nd attempt (higher timeout)
     *
     * Helps mitigate occasional rate-limits, slow CDN edges, or transient failures.
     */
    private Optional<Document> safeGet(String url) {
        try
        {
            return Optional.of(
                    Jsoup.connect(url)
                            .userAgent(USER_AGENT)
                            .timeout(TIMEOUT)
                            .referrer("https://google.com")
                            .get()
            );
        }
        catch (Exception e)
        {

            log.warn("Jsoup get failed for {}, retrying...", url);

            try
            {
                Thread.sleep(1200);
            }
            catch (InterruptedException ignored)
            {}
            try
            {
                return Optional.of(
                        Jsoup.connect(url)
                                .userAgent(USER_AGENT)
                                .timeout(TIMEOUT * 2)
                                .referrer("https://google.com")
                                .get()
                );
            }
            catch (Exception ex)
            {
                log.error("Jsoup retry failed for {}", url, ex);
                return Optional.empty();
            }
        }
    }

    /**
     * Scrape Amazon search results + first product page.
     *
     * Amazon is the most unpredictable among vendors due to:
     *  - A/B testing layouts
     *  - Sponsored product placements
     *  - Region-tuned HTML differences
     */
    public Optional<SmartphonePriceResult> scrapeAmazon(String query)
    {
        try
        {
            String url = "https://www.amazon.in/s?k=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Document searchDoc = safeGet(url).orElse(null);
            if (searchDoc == null) return Optional.empty();

            // Prefer official product tiles, fallback to generic result-item structure.
            Elements tiles = searchDoc.select("div[data-index]");
            Element first = !tiles.isEmpty()
                    ? tiles.first()
                    : searchDoc.selectFirst("div.s-result-item");

            if (first == null) return Optional.empty();

            Element linkEl = first.selectFirst("h2 a.a-link-normal, a.a-link-normal.s-no-outline");
            if (linkEl == null) return Optional.empty();

            String href = linkEl.attr("href");
            String productUrl = href.startsWith("http")
                    ? href
                    : "https://www.amazon.in" + href;

            Document prodDoc = safeGet(productUrl).orElse(null);
            if (prodDoc == null) return Optional.empty();

            // Robust multi-selector price fallback
            String priceText =
                    Optional.ofNullable(prodDoc.selectFirst("#priceblock_dealprice"))
                            .or(() -> Optional.ofNullable(prodDoc.selectFirst("#priceblock_ourprice")))
                            .or(() -> Optional.ofNullable(prodDoc.selectFirst(".a-price .a-offscreen")))
                            .or(() -> Optional.ofNullable(prodDoc.selectFirst("span.offer-price")))
                            .map(Element::text)
                            .orElse(null);

            String title =
                    Optional.ofNullable(prodDoc.selectFirst("#productTitle"))
                            .map(Element::text)
                            .orElse(query);

            boolean inStock =
                    Optional.ofNullable(prodDoc.selectFirst("#availability, .a-size-medium.a-color-success"))
                            .map(e -> !e.text().toLowerCase().contains("out of stock"))
                            .orElse(true);

            Double price = priceText != null ? parsePrice(priceText) : null;
            String imageUrl = getImageUrl(prodDoc);
            Double rating = getRating(prodDoc);

            return price != null
                    ? Optional.of(new SmartphonePriceResult("Amazon", price, productUrl, title, inStock, imageUrl, rating))
                    : Optional.empty();

        }
        catch (Exception ex)
        {
            log.error("Amazon scraping failed for query={}", query, ex);
            return Optional.empty();
        }
    }

    /**
     * Flipkart scraper.
     *
     * Flipkart HTML is more stable than Amazon, but class names are obfuscated
     * and change without announcement. We rely on the most consistent parent containers.
     */
    public Optional<SmartphonePriceResult> scrapeFlipkart(String query)
    {
        try
        {
            String url = "https://www.flipkart.com/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Document searchDoc = safeGet(url).orElse(null);
            if (searchDoc == null) return Optional.empty();

            Element first = searchDoc.selectFirst("a._1fQZEK, div._2kHMtA");
            if (first == null) return Optional.empty();

            String href = first.attr("href");
            if (href == null) return Optional.empty();

            String productUrl = href.startsWith("http")
                    ? href
                    : "https://www.flipkart.com" + href;

            Document prodDoc = safeGet(productUrl).orElse(null);
            if (prodDoc == null) return Optional.empty();

            Element priceEl = prodDoc.selectFirst("div._30jeq3");

            String priceText = priceEl != null ? priceEl.text() : null;

            String title =
                    Optional.ofNullable(prodDoc.selectFirst("span.B_NuCI"))
                            .map(Element::text)
                            .orElse(query);

            boolean inStock = true;
            Element outEl = prodDoc.selectFirst("div._16FRp0");
            if (outEl != null && outEl.text().toLowerCase().contains("out of stock"))
                inStock = false;

            Double price = priceText != null ? parsePrice(priceText) : null;

            String imageUrl = getImageUrl(prodDoc);
            Double rating = getRating(prodDoc);

            return price != null
                    ? Optional.of(new SmartphonePriceResult("Flipkart", price, productUrl, title, inStock, imageUrl, rating))
                    : Optional.empty();

        }
        catch (Exception ex)
        {
            log.error("Flipkart scraping failed for query={}", query, ex);
            return Optional.empty();
        }
    }

    /**
     * Croma scraper.
     *
     * Croma layouts are relatively stable, but their PDP pages sometimes
     * delay-render price using JS. JSoup fetch still works for most SKUs.
     */
    public Optional<SmartphonePriceResult> scrapeCroma(String query)
    {
        try
        {
            String url = "https://www.croma.com/search/?text=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Document searchDoc = safeGet(url).orElse(null);
            if (searchDoc == null) return Optional.empty();

            Element first = searchDoc.selectFirst("div.product-block a, div.item a");
            if (first == null) return Optional.empty();

            String href = first.attr("href");
            if (href == null || href.isBlank()) return Optional.empty();

            String productUrl = href.startsWith("http")
                    ? href
                    : "https://www.croma.com" + href;

            Document prodDoc = safeGet(productUrl).orElse(null);
            if (prodDoc == null) return Optional.empty();

            Element priceEl =
                    prodDoc.selectFirst("span[itemprop=price], .pdPrice, .pdpPrice, .product-price");

            String priceText = priceEl != null ? priceEl.text() : null;

            String title =
                    Optional.ofNullable(prodDoc.selectFirst("h1.product-title"))
                            .map(Element::text)
                            .orElse(query);

            boolean inStock = true;
            Element stockEl = prodDoc.selectFirst(".availability");
            if (stockEl != null && stockEl.text().toLowerCase().contains("out of stock"))
                inStock = false;

            Double price = priceText != null ? parsePrice(priceText) : null;

            String imageUrl = getImageUrl(prodDoc);
            Double rating = getRating(prodDoc);

            return price != null
                    ? Optional.of(new SmartphonePriceResult("Croma", price, productUrl, title, inStock, imageUrl, rating))
                    : Optional.empty();

        }
        catch (Exception ex)
        {
            log.error("Croma scraping failed for query={}", query, ex);
            return Optional.empty();
        }
    }

    /**
     * Price formatter that normalizes vendor-specific price formats:
     *  - Removes non-numeric characters
     *  - Supports comma-heavy Indian formatting ("1,49,999")
     *  - Handles decimal anomalies and multiple dots
     */
    private Double parsePrice(String text)
    {
        if (text == null) return null;

        try
        {
            String cleaned = text.replaceAll("[^0-9.,]", "")
                    .replaceAll(",", "");

            if (cleaned.isBlank()) return null;

            int firstDot = cleaned.indexOf('.');
            if (firstDot >= 0) {
                String left = cleaned.substring(0, firstDot);
                String right = cleaned.substring(firstDot + 1)
                        .replaceAll("\\.", ""); // remove extra dots

                cleaned = left + "." + right;
            }

            return Double.parseDouble(cleaned);

        }
        catch (Exception ex)
        {
            log.warn("parsePrice error for text='{}' -> {}", text, ex.getMessage());
            return null;
        }
    }
}
