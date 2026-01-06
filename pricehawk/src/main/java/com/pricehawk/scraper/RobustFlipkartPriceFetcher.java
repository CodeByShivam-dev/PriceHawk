package com.pricehawk.scraper;

import com.pricehawk.dto.SmartphonePriceResult;
import java.util.Collections;
import java.util.List;
/**
 * Flipkart scraping is unstable due to frequent DOM changes.
 * This wrapper tries a primary strategy first and falls back
 * to a simpler selector-based scraper if needed.
 */

public class RobustFlipkartPriceFetcher implements PriceFetcher
{

    private final FlipkartPriceFetcher primary = new FlipkartPriceFetcher();
    private final SimpleSelectorFlipkartFetcher fallback = new SimpleSelectorFlipkartFetcher();

    @Override
    public List<SmartphonePriceResult> fetchPrices(String query)
    {
        try
        {
            List<SmartphonePriceResult> results = primary.fetchPrices(query);
            if (results != null && !results.isEmpty()) return results;
        }
        catch (Exception ignored)
        {}// Primary selector broke — happens often with Flipkart

        try
        {
            List<SmartphonePriceResult> fallbackResults = fallback.fetchPrices(query);
            if (fallbackResults != null && !fallbackResults.isEmpty()) return fallbackResults;
        }
        catch (Exception ignored)
        {} // Both attempts failed; handled gracefully below

        return Collections.emptyList();
    }
}
