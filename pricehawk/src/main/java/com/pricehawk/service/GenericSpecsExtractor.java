package com.pricehawk.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Generic specs extractor used as a last-resort fallback.
 *
 * Designed for pages where structured APIs or site-specific selectors
 * are unavailable or unreliable. Works by scanning common HTML patterns
 * (tables / bullet lists) and filtering meaningful specs via keywords.
 */
@Slf4j
public class GenericSpecsExtractor
{
    /**
     * Broad keyword set to catch most smartphone spec variations
     * across different e-commerce and review sites.
     */
    private static final String[] KEYWORDS =
            {
            "ram", "storage", "rom", "memory",
            "display", "screen",
            "camera", "rear", "front",
            "battery", "mah",
            "processor", "chipset"
    };

    /**
     * Attempts to extract a compact, human-readable specs summary
     * from a product detail page.
     *
     * The method intentionally prefers resilience over precision,
     * as DOM structures tend to vary frequently across sites.
     */
    public String extract(Document doc)
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            // 1) First preference: structured tables (most reliable when present)
            pickFromCollection(sb, doc.select("table tr"));

            // 2) Fallback: unordered lists commonly used for highlights/specs
            if (sb.length() < 1)
            {
                pickFromCollection(sb, doc.select("ul li"));
            }

            String summary = sb.toString();
            return summary.isBlank() ? null : summary;

        }
        catch (Exception e)
        {
            // Never fail the entire scraping pipeline due to specs extraction
            log.warn("GenericSpecsExtractor failed", e);
            return null;
        }
    }

    /**
     * Iterates over a collection of DOM elements and selectively
     * appends lines that look like actual device specifications.
     */
    private void pickFromCollection(StringBuilder sb, Elements elements)
    {
        for (Element el : elements)
        {
            // Hard stop to keep output concise and UI-friendly
            if (sb.length() > 180) break; // limit summary length
            String text = el.text();
            if (text == null || text.isBlank()) continue;

            String lower = text.toLowerCase();
            boolean important = false;

            // Keyword-based filtering to avoid marketing noise
            for (String kw : KEYWORDS)
            {
                if (lower.contains(kw))
                {
                    important = true;
                    break;
                }
            }
            if (!important) continue;

            append(sb, text);
        }
    }

    /**
     * Appends specs using a compact separator so that
     * the final output reads like a short summary, not raw HTML.
     */
    private void append(StringBuilder sb, String value)
    {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append(" · ");
        sb.append(value.trim());
    }
}
