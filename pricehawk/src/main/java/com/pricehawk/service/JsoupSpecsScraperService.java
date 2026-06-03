package com.pricehawk.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Fetches product specification summaries directly from product pages.
 *
 * Different marketplaces expose specifications using different HTML
 * structures, so extraction is delegated to marketplace-specific
 * extractors whenever possible.
 */
@Service
@Slf4j
public class JsoupSpecsScraperService implements PhoneSpecsService.SpecsScraperService
{

    private final FlipkartSpecsExtractor flipkartExtractor =
            new FlipkartSpecsExtractor();

    private final AmazonSpecsExtractor amazonExtractor =
            new AmazonSpecsExtractor();

    /*
     * Safety net for unsupported stores or future sources.
     * Tries to identify useful specs using generic keyword matching.
     */
    private final GenericSpecsExtractor genericExtractor =
            new GenericSpecsExtractor();

    private static final int TIMEOUT_MS =
            (int) Duration.ofSeconds(8).toMillis();

    @Override
    public String scrapeSpecs(String productUrl)
    {
        if (productUrl == null || productUrl.isBlank())
        {
            return null;
        }

        try
        {
            Document doc = Jsoup.connect(productUrl)
                    .userAgent("Mozilla/5.0 (PriceHawk Specs Bot)")
                    .timeout(TIMEOUT_MS)
                    .get();

            /*
             * Using the final resolved URL instead of the original input
             * helps when marketplaces redirect through tracking or
             * shortened links.
             */
            String resolvedUrl = doc.location();

            String summary;

            if (resolvedUrl.contains("flipkart.com"))
            {
                summary = flipkartExtractor.extract(doc);
            }
            else if (resolvedUrl.contains("amazon.in"))
            {
                summary = amazonExtractor.extract(doc);
            }
            else
            {
                summary = genericExtractor.extract(doc);
            }

            if (summary != null)
            {
                summary = summary.trim();
            }

            return (summary == null || summary.isEmpty())
                    ? null
                    : summary;
        }
        catch (Exception e)
        {
            /*
             * Spec enrichment is a secondary feature and should never
             * block the main pricing workflow.
             */
            log.warn("Specs scraping failed for url={}", productUrl, e);
            return null;
        }
    }
}