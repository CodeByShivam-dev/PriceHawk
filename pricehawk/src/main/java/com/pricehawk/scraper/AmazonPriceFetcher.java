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
 * Amazon scraper backed by Selenium.
 *
 * Why Selenium?
 * Amazon heavily relies on dynamic rendering and anti-bot measures.
 * Jsoup alone tends to fail intermittently → Selenium stabilizes extraction.
 *
 * Design goals:
 * - survive minor DOM changes (fallback selectors)
 * - avoid aggressive scraping patterns
 * - fail gracefully (never crash upstream flow)
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

        // headless is enough here, full browser not needed
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        // consistent viewport → avoids layout-based selector issues
        options.addArguments("--window-size=1920,1080");

        // rotating UA helps reduce trivial bot flags (not foolproof)
        options.addArguments("--user-agent=" + getRandomUserAgent());

        this.driver = new ChromeDriver(options);

        // keep implicit wait minimal — we rely on retry instead
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query)
    {
        List<SmartphonePriceResult> results = new ArrayList<>();

        String url = "https://www.amazon.in/s?k=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++)
        {
            try
            {
                // small jitter to avoid predictable request patterns
                Thread.sleep(1000 + RANDOM.nextInt(2000));

                driver.get(url);

                // we still parse via Jsoup for easier selector handling
                String pageHtml = driver.getPageSource();
                Document doc = Jsoup.parse(pageHtml);

                // iterate over visible search results
                for (Element card : doc.select("div[data-component-type='s-search-result']"))
                {
                    Element linkElem = card.selectFirst("a.a-link-normal.s-no-outline");
                    if (linkElem == null) continue;

                    String href = linkElem.attr("href");
                    String fullLink = href.startsWith("http")
                            ? href
                            : "https://www.amazon.in" + href;

                    Element priceElem = card.selectFirst("span.a-price span.a-offscreen");
                    if (priceElem == null) continue;

                    Double price = parsePrice(priceElem.text());
                    if (price == null) continue;

                    Element titleElem = card.selectFirst("span.a-size-medium, span.a-size-base-plus");
                    String title = titleElem != null ? titleElem.text() : query;

                    // rating is optional → do not fail extraction for it
                    Double rating = null;
                    Element ratingElem = card.selectFirst("span.a-icon-alt");
                    if (ratingElem != null)
                    {
                        try
                        {
                            String text = ratingElem.text().split(" ")[0];
                            rating = Double.parseDouble(text.replaceAll("[^0-9.]", ""));
                        }
                        catch (Exception ignored) {}
                    }

                    // quick heuristic (not perfect but cheap)
                    boolean inStock = !card.text().toLowerCase().contains("unavailable");

                    results.add(new SmartphonePriceResult(
                            "Amazon",
                            price,
                            fullLink,
                            title,
                            inStock,
                            getImageUrl(card),
                            rating,
                            null
                    ));

                    // we only need the best/top result → exit early
                    return results;
                }

                // no usable cards → no point retrying same DOM repeatedly
                break;
            }
            catch (Exception e)
            {
                log.warn("Attempt {} failed for query '{}': {}", attempt, query, e.getMessage());

                try
                {
                    // basic exponential backoff
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
            String cleaned = raw.replaceAll("[^0-9.]", "").replace(",", "");
            if (cleaned.isBlank()) return null;

            return Double.parseDouble(cleaned);
        }
        catch (Exception e)
        {
            // silent fail — bad price shouldn't break pipeline
            return null;
        }
    }

    private String getImageUrl(Element card)
    {
        Element img = card.selectFirst("img.s-image");

        // fallback ensures UI never breaks
        return img != null
                ? img.attr("src")
                : "https://via.placeholder.com/300.png?text=Amazon+Phone";
    }

    /**
     * Not a strong anti-bot measure, but helps avoid naive blocking.
     * Real systems would rotate proxies + headers.
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
     * Important: driver should be closed by caller/service lifecycle.
     * Otherwise Chrome instances will leak.
     */
    public void close()
    {
        if (driver != null)
        {
            driver.quit();
        }
    }
}