package com.pricehawk.scraper;

import com.pricehawk.dto.SmartphonePriceResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight Flipkart scraper using relaxed selectors.
 *
 * This acts as a fallback when the primary scraper fails
 * (usually due to DOM structure changes or anti-bot variations).
 *
 * Trade-offs:
 * - Less strict selectors → higher chance of partial matches
 * - No dependency on exact class names (which Flipkart frequently changes)
 * - More tolerant, but slightly less accurate
 */
public class SimpleSelectorFlipkartFetcher implements PriceFetcher
{

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query)
    {
        List<SmartphonePriceResult> results = new ArrayList<>();

        try
        {
            String url = "https://www.flipkart.com/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .referrer("https://google.com")
                    .get();

            // Flipkart often uses dynamic classes → data-id is relatively stable
            for (Element card : doc.select("div[data-id]"))
            {
                // Try to extract product link (title-based first, fallback to generic <a>)
                Element link = card.selectFirst("a[title]");
                if (link == null)
                {
                    link = card.selectFirst("a");
                }

                // Price usually appears as text containing ₹ symbol
                Element priceEl = card.selectFirst("div:matchesOwn(₹)");

                if (link == null || priceEl == null)
                {
                    continue; // skip incomplete cards
                }

                String href = link.attr("href");
                if (href == null || href.isBlank())
                {
                    continue;
                }

                String fullLink = href.startsWith("http")
                        ? href
                        : "https://www.flipkart.com" + href;

                Double price = parse(priceEl.text());
                if (price == null)
                {
                    continue;
                }

                String title = link.attr("title");
                if (title == null || title.isBlank())
                {
                    title = link.text();
                }
                if (title == null || title.isBlank())
                {
                    title = query;
                }

                results.add(
                        new SmartphonePriceResult(
                                "Flipkart",
                                price,
                                fullLink,
                                title,
                                true,
                                null,
                                null,
                                null
                        )
                );

                // keep it small → aggregator doesn’t need full page scrape
                if (results.size() >= 2)
                {
                    break;
                }
            }
        }
        catch (Exception ignored)
        {
            // fail quietly → upstream aggregator handles fallback logic
        }

        return results;
    }

    private Double parse(String p)
    {
        try
        {
            String cleaned = p.replaceAll("[^0-9.,]", "")
                    .replace(",", "")
                    .trim();

            if (cleaned.isBlank())
            {
                return null;
            }

            return Double.parseDouble(cleaned);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}