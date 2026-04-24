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
 * Flipkart scraper using Selenium.
 *
 * Similar approach as Amazon scraper:
 * - handles dynamic content reliably
 * - keeps extraction logic tolerant to small DOM changes
 *
 * Note: Flipkart layout changes more frequently than Amazon,
 * so selectors here are intentionally a bit defensive.
 */
@Slf4j
public class FlipkartPriceFetcher implements PriceFetcher
{

    private static final int MAX_RETRIES = 3;
    private static final Random RANDOM = new Random();

    private final WebDriver driver;

    public FlipkartPriceFetcher()
    {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        // consistent viewport helps avoid layout-based selector breakage
        options.addArguments("--window-size=1920,1080");

        // basic UA rotation (not a full anti-bot solution)
        options.addArguments("--user-agent=" + getRandomUserAgent());

        this.driver = new ChromeDriver(options);

        // rely more on retries than long waits
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query)
    {
        List<SmartphonePriceResult> results = new ArrayList<>();

        String url = "https://www.flipkart.com/search?q=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++)
        {
            try
            {
                // small delay to reduce request burst pattern
                Thread.sleep(1000 + RANDOM.nextInt(2000));

                driver.get(url);

                String pageHtml = driver.getPageSource();
                Document doc = Jsoup.parse(pageHtml);

                // Flipkart uses multiple layouts → keep selector slightly generic
                for (Element card : doc.select("div._1AtVbE"))
                {
                    Element linkElem = card.selectFirst("a._1fQZEK");
                    if (linkElem == null) continue;

                    String href = linkElem.attr("href");
                    String fullLink = href.startsWith("http")
                            ? href
                            : "https://www.flipkart.com" + href;

                    Element priceElem = card.selectFirst("div._30jeq3");
                    if (priceElem == null) continue;

                    Double price = parsePrice(priceElem.text());
                    if (price == null) continue;

                    Element titleElem = card.selectFirst("div._4rR01T");
                    String title = titleElem != null ? titleElem.text() : query;

                    // rating is optional — don't block extraction
                    Double rating = null;
                    Element ratingElem = card.selectFirst("div._3LWZlK");
                    if (ratingElem != null)
                    {
                        try
                        {
                            rating = Double.parseDouble(ratingElem.text());
                        }
                        catch (Exception ignored) {}
                    }

                    // simple heuristic (cheap but not always accurate)
                    boolean inStock = !card.text().toLowerCase().contains("out of stock");

                    results.add(new SmartphonePriceResult(
                            "Flipkart",
                            price,
                            fullLink,
                            title,
                            inStock,
                            getImageUrl(card),
                            rating,
                            null
                    ));

                    // only top result needed → exit early
                    return results;
                }

                // DOM parsed but no usable product → no need to retry same structure
                break;
            }
            catch (Exception e)
            {
                log.warn("Attempt {} failed for Flipkart query '{}': {}",
                        attempt, query, e.getMessage());

                try
                {
                    // simple backoff strategy
                    Thread.sleep(2000L * attempt);
                }
                catch (InterruptedException ignored) {}
            }
        }

        return results;
    }

    private Double parsePrice(String raw)
    {
        try
        {
            // Flipkart price comes like ₹49,999 → strip non-digits
            String cleaned = raw.replaceAll("[^0-9]", "");

            if (cleaned.isBlank()) return null;

            return Double.parseDouble(cleaned);
        }
        catch (Exception e)
        {
            // do not propagate parsing failures
            return null;
        }
    }

    private String getImageUrl(Element card)
    {
        Element img = card.selectFirst("img._396cs4");

        // fallback ensures UI consistency
        return img != null
                ? img.attr("src")
                : "https://via.placeholder.com/300.png?text=Flipkart+Phone";
    }

    /**
     * Lightweight UA rotation.
     * Helps against basic blocking but not sufficient for large-scale scraping.
     */
    private String getRandomUserAgent()
    {
        String[] agents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Safari/605.1.15",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        };

        return agents[RANDOM.nextInt(agents.length)];
    }

    /**
     * Must be called when scraper is no longer needed.
     * Prevents orphan Chrome processes.
     */
    public void close()
    {
        if (driver != null)
        {
            driver.quit();
        }
    }
}