package com.pricehawk.scraper;

import com.pricehawk.dto.SmartphonePriceResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Amazon-specific price fetcher using Selenium + Jsoup.
 *
 * Features:
 * - Headless Chrome to handle JS-loaded pages
 * - Retry logic with exponential backoff
 * - Random delays to reduce bot detection
 * - Fallback selectors for robustness
 */
@Slf4j
public class AmazonPriceFetcher implements PriceFetcher
{

    private static final int MAX_RETRIES = 3;
    private static final Random RANDOM = new Random();

    private final WebDriver driver;

    public AmazonPriceFetcher()
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");  // Headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=" + getRandomUserAgent());

        this.driver = new ChromeDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query) {
        List<SmartphonePriceResult> results = new ArrayList<>();
        String url = "https://www.amazon.in/s?k=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Random small delay to mimic human behavior
                Thread.sleep(1000 + RANDOM.nextInt(2000));

                driver.get(url);
                String pageHtml = driver.getPageSource();
                Document doc = Jsoup.parse(pageHtml);

                // Multiple result fallback
                for (Element card : doc.select("div[data-component-type='s-search-result']")) {

                    // Product link
                    Element linkElem = card.selectFirst("a.a-link-normal.s-no-outline");
                    if (linkElem == null) continue;
                    String href = linkElem.attr("href");
                    String fullLink = href.startsWith("http") ? href : "https://www.amazon.in" + href;

                    // Price
                    Element priceElem = card.selectFirst("span.a-price span.a-offscreen");
                    if (priceElem == null) continue;
                    Double price = parsePrice(priceElem.text());
                    if (price == null) continue;

                    // Title
                    Element titleElem = card.selectFirst("span.a-size-medium, span.a-size-base-plus");
                    String title = titleElem != null ? titleElem.text() : query;

                    // Rating
                    Double rating = null;
                    Element ratingElem = card.selectFirst("span.a-icon-alt");
                    if (ratingElem != null) {
                        try {
                            String text = ratingElem.text().split(" ")[0];
                            rating = Double.parseDouble(text.replaceAll("[^0-9.]", ""));
                        } catch (Exception ignored) {}
                    }

                    // Stock check
                    boolean inStock = !card.text().toLowerCase().contains("unavailable");

                    results.add(new SmartphonePriceResult(
                            "Amazon", price, fullLink, title, inStock,
                            getImageUrl(card), rating, null
                    ));

                    // Return first valid match
                    return results;
                }

                // No valid results, break retry loop
                break;

            } catch (Exception e) {
                log.warn("Attempt {} failed for query '{}': {}", attempt, query, e.getMessage());
                try {
                    Thread.sleep(2000L * attempt);  // exponential backoff
                } catch (InterruptedException ignored) {}
            }
        }

        return results;
    }

    private Double parsePrice(String p) {
        try {
            String cleaned = p.replaceAll("[^0-9.]", "").replace(",", "");
            if (cleaned.isBlank()) return null;
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private String getImageUrl(Element card) {
        Element img = card.selectFirst("img.s-image");
        return img != null ? img.attr("src") : "https://via.placeholder.com/300.png?text=Amazon+Phone";
    }

    private String getRandomUserAgent() {
        String[] agents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Safari/605.1.15",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        };
        return agents[RANDOM.nextInt(agents.length)];
    }

    public void close() {
        if (driver != null) driver.quit();
    }
}