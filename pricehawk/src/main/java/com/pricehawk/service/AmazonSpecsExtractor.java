package com.pricehawk.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Extracts a concise, human-readable specs summary from Amazon product pages.
 * Focuses only on high-signal specs to avoid noisy or marketing-heavy content.
 */
@Slf4j
public class AmazonSpecsExtractor
{
    /**
     * Parses the product HTML and builds a compact specs string
     * from the "About this item" section.
     */
    public String extract(Document doc)
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            // Primary selector targeting Amazon's feature bullets
            Elements items = doc.select("div#feature-bullets ul.a-unordered-list.a-vertical.a-spacing-mini li.a-spacing-mini");

            // Fallback selector: Amazon DOM changes frequently
            if (items.isEmpty())
            {
                // fallback: thoda generic agar structure thoda change ho
                items = doc.select("div#feature-bullets li");
            }

            for (Element li : items)
            {
                String text = li.text();

                // Filter only meaningful hardware-related specs
                if (isImportant(text))
                {
                    append(sb, text);
                }
            }

            String summary = sb.toString();
            return summary.isBlank() ? null : summary;

        }
        catch (Exception e)
        // Non-fatal: specs extraction should never break the price flow
        {
            log.warn("AmazonSpecsExtractor failed", e);
            return null;
        }
    }

    /**
     * Lightweight heuristic to keep only high-value technical specs.
     * Avoids over-engineering with NLP for stability and speed.
     */
    private boolean isImportant(String text)
    {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("ram")
                || lower.contains("rom")
                || lower.contains("storage")
                || lower.contains("display")
                || lower.contains("screen")
                || lower.contains("camera")
                || lower.contains("battery")
                || lower.contains("mah")
                || lower.contains("processor")
                || lower.contains("chip")
                || lower.contains("snapdragon")
                || lower.contains("bionic");
    }
    /**
     * Appends specs in a readable single-line format.
     * Uses separators to keep UI-friendly summaries.
     */
    private void append(StringBuilder sb, String value)
    {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append(" · ");
        sb.append(value.trim());
    }
}
