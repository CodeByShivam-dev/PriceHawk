package com.pricehawk.service;

import com.pricehawk.dto.SmartphonePriceResult;
import org.jsoup.Connection;
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
 * - scrapeAmazon(query)
 * - scrapeFlipkart(query)
 * - scrapeCroma(query)
 *
 * Each method returns Optional<SmartphonePriceResult> (empty => failure)
 *
 * NOTE: scraping e-commerce sites can be brittle; the code uses multiple fallbacks.
 */
@Service
public class PriceScraperService {

    private static final Logger log = LoggerFactory.getLogger(PriceScraperService.class);

    // Common headers to reduce bot detection
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 10000; // 10s

    // --------- AMAZON ----------
    public Optional<SmartphonePriceResult> scrapeAmazon(String query) {
        try {
            String searchUrl = "https://www.amazon.in/s?k=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            log.info("Amazon search URL: {}", searchUrl);

            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .referrer("https://www.google.com")
                    .get();

            // Find first product tile element
            Element first = null;
            // Common selector: search results have div[data-index]
            Elements tiles = searchDoc.select("div[data-index]");
            if (!tiles.isEmpty()) first = tiles.first();

            if (first == null) {
                // fallback: older structure
                first = searchDoc.selectFirst("div.s-result-item");
            }

            if (first == null) {
                log.warn("Amazon: no product tile found for query={}", query);
                return Optional.empty();
            }

            // Find product link
            Element linkEl = first.selectFirst("h2 a.a-link-normal, a.a-link-normal.s-no-outline");
            String href = linkEl != null ? linkEl.attr("href") : null;
            if (href == null || href.isBlank()) {
                // fallback: older card link
                Element alt = first.selectFirst("a.a-link-normal");
                href = alt != null ? alt.attr("href") : null;
            }

            if (href == null) {
                log.warn("Amazon: product link not found");
                return Optional.empty();
            }

            String productUrl = href.startsWith("http") ? href : "https://www.amazon.in" + href;
            // fetch product page (more reliable price)
            Document prodDoc = Jsoup.connect(productUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .referrer(searchUrl)
                    .get();

            // Try multiple selectors for price on product page
            String priceText = null;
            Element priceEl;

            priceEl = prodDoc.selectFirst("#priceblock_dealprice");
            if (priceEl == null) priceEl = prodDoc.selectFirst("#priceblock_ourprice");
            if (priceEl == null) priceEl = prodDoc.selectFirst(".a-price .a-offscreen");
            if (priceEl == null) priceEl = prodDoc.selectFirst("span.offer-price"); // fallback
            if (priceEl != null) priceText = priceEl.text();

            // title
            String title = Optional.ofNullable(prodDoc.selectFirst("#productTitle"))
                    .map(Element::text).orElseGet(() ->
                            Optional.ofNullable(first.selectFirst("h2 a.a-link-normal span")).map(Element::text).orElse(query));

            boolean inStock = true;
            Element stockEl = prodDoc.selectFirst("#availability, .a-size-medium.a-color-success");
            if (stockEl != null) {
                String s = stockEl.text().toLowerCase();
                if (s.contains("out of stock") || s.contains("currently unavailable")) inStock = false;
            }

            if (priceText == null) {
                // fallback: maybe price available in search tile
                Element tilePrice = first.selectFirst(".a-price .a-offscreen, .a-price-whole");
                if (tilePrice != null) priceText = tilePrice.text();
            }

            if (priceText == null) {
                log.warn("Amazon: price not found for productUrl={}", productUrl);
                return Optional.empty();
            }

            Double price = parsePrice(priceText);
            if (price == null) {
                log.warn("Amazon: unable to parse price text='{}' for {}", priceText, productUrl);
                return Optional.empty();
            }

            SmartphonePriceResult res = new SmartphonePriceResult("Amazon", price, productUrl, title, inStock);
            return Optional.of(res);

        } catch (Exception ex) {
            log.error("Amazon scraping failed for query={}", query, ex);
            return Optional.empty();
        }
    }

    // --------- FLIPKART ----------
    public Optional<SmartphonePriceResult> scrapeFlipkart(String query) {
        try {
            String searchUrl = "https://www.flipkart.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            log.info("Flipkart search URL: {}", searchUrl);

            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .referrer("https://www.google.com")
                    .get();

            // Flipkart product tiles: check a few selectors
            Element first = searchDoc.selectFirst("a.s1Q9rs, a._1fQZEK, div._4rR01T"); // links or tile variations
            String href = null;
            if (first != null) {
                // if 'a' element selected, may contain href directly
                href = first.tagName().equals("a") ? first.attr("href") : first.parent() != null ? first.parent().attr("href") : null;
            } else {
                Element alt = searchDoc.selectFirst("a._2rpwqI"); // fallback
                if (alt != null) href = alt.attr("href");
            }

            if (href == null) {
                log.warn("Flipkart: product link not found for query={}", query);
                return Optional.empty();
            }

            String productUrl = href.startsWith("http") ? href : "https://www.flipkart.com" + href;
            Document prodDoc = Jsoup.connect(productUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .referrer(searchUrl)
                    .get();

            // Price selectors on product page (common)
            Element priceEl = prodDoc.selectFirst("div._30jeq3._16Jk6d, div._30jeq3"); // common Flipkart price class
            String priceText = priceEl != null ? priceEl.text() : null;

            // Title
            String title = Optional.ofNullable(prodDoc.selectFirst("span.B_NuCI")).map(Element::text)
                    .orElse(Optional.ofNullable(prodDoc.selectFirst("h1")).map(Element::text).orElse(query));

            boolean inStock = true;
            Element outEl = prodDoc.selectFirst("div._16FRp0"); // out of stock class sometimes
            if (outEl != null && outEl.text().toLowerCase().contains("out of stock")) inStock = false;

            if (priceText == null) {
                // fallback: check search tile price
                Element tilePrice = searchDoc.selectFirst("div._30jeq3");
                if (tilePrice != null) priceText = tilePrice.text();
            }

            if (priceText == null) {
                log.warn("Flipkart: price not found for {}", productUrl);
                return Optional.empty();
            }

            Double price = parsePrice(priceText);
            if (price == null) {
                log.warn("Flipkart: cannot parse price text='{}'", priceText);
                return Optional.empty();
            }

            SmartphonePriceResult res = new SmartphonePriceResult("Flipkart", price, productUrl, title, inStock);
            return Optional.of(res);

        } catch (Exception ex) {
            log.error("Flipkart scraping failed for query={}", query, ex);
            return Optional.empty();
        }
    }

    // --------- CROMA ----------
    public Optional<SmartphonePriceResult> scrapeCroma(String query) {
        try {
            String searchUrl = "https://www.croma.com/search/?text=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            log.info("Croma search URL: {}", searchUrl);

            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .referrer("https://www.google.com")
                    .get();

            Element first = searchDoc.selectFirst("div.product-block a, div.item a"); // tile link
            if (first == null) {
                log.warn("Croma: no product tile found for query={}", query);
                return Optional.empty();
            }

            String href = first.attr("href");
            if (href == null || href.isBlank()) {
                log.warn("Croma: product link not found in tile");
                return Optional.empty();
            }
            String productUrl = href.startsWith("http") ? href : "https://www.croma.com" + href;

            Document prodDoc = Jsoup.connect(productUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .referrer(searchUrl)
                    .get();

            // price selectors: try itemprop or class names
            Element priceEl = prodDoc.selectFirst("span[itemprop=price], .pdPrice, .pdpPrice, .product-price");
            String priceText = priceEl != null ? priceEl.text() : null;

            String title = Optional.ofNullable(prodDoc.selectFirst("h1.product-title"))
                    .map(Element::text)
                    .orElse(Optional.ofNullable(prodDoc.selectFirst("h1")).map(Element::text).orElse(query));

            boolean inStock = true;
            Element stockEl = prodDoc.selectFirst(".availability");
            if (stockEl != null && stockEl.text().toLowerCase().contains("out of stock")) inStock = false;

            if (priceText == null) {
                // fallback: maybe search tile shows price
                Element tilePrice = searchDoc.selectFirst(".product-price, .price");
                if (tilePrice != null) priceText = tilePrice.text();
            }

            if (priceText == null) {
                log.warn("Croma: price not found for {}", productUrl);
                return Optional.empty();
            }

            Double price = parsePrice(priceText);
            if (price == null) {
                log.warn("Croma: parse failed for '{}'", priceText);
                return Optional.empty();
            }

            SmartphonePriceResult res = new SmartphonePriceResult("Croma", price, productUrl, title, inStock);
            return Optional.of(res);

        } catch (Exception ex) {
            log.error("Croma scraping failed for query={}", query, ex);
            return Optional.empty();
        }
    }

    // ---------- Utility: parse price string like "₹ 73,999" or "Rs. 73,999" or "73,999" ----------
    private Double parsePrice(String text) {
        if (text == null) return null;
        try {
            // remove currency symbols, commas, non-digit except dot
            String cleaned = text.replaceAll("[^0-9.,]", "");
            // There might be formats like "73,999" or "73,999.00"
            cleaned = cleaned.replaceAll(",", "");
            if (cleaned.isBlank()) return null;
            // handle multiple dots (rare): keep left-most dot as decimal
            int firstDot = cleaned.indexOf('.');
            if (firstDot >= 0) {
                String left = cleaned.substring(0, firstDot);
                String right = cleaned.substring(firstDot + 1).replaceAll("\\.", "");
                cleaned = left + "." + right;
            }
            return Double.parseDouble(cleaned);
        } catch (Exception ex) {
            log.warn("parsePrice error for text='{}' -> {}", text, ex.getMessage());
            return null;
        }
    }
}
