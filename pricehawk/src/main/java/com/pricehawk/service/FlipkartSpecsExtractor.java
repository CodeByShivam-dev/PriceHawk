package com.pricehawk.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Flipkart specific specs extraction.
 * NOTE: CSS selectors ko page inspect karke adjust karna padega.
 */
@Slf4j
class FlipkartSpecsExtractor
{

    public String extract(Document doc)
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            // 1) Highlights list (RAM, storage, camera, processor, battery)
            Elements items = doc.select("li.L5SqY1"); // ya "div.qfnhAa ~ ul li.L5SqY1"
            for (Element li : items)
            {
                String text = li.text();
                if (isImportant(text))
                {
                    append(sb, text);
                }
            }

            String summary = sb.toString();
            return summary.isBlank() ? null : summary;

        }
        catch (Exception e)

        {
            log.warn("FlipkartSpecsExtractor failed", e);
            return null;
        }
    }

    private boolean isImportant(String text)
    {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("ram")
                || lower.contains("rom")
                || lower.contains("storage")
                || lower.contains("display")
                || lower.contains("camera")
                || lower.contains("battery")
                || lower.contains("bionic")
                || lower.contains("snapdragon")
                || lower.contains("chip");
    }

    private void append(StringBuilder sb, String value)
    {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append(" · ");
        sb.append(value.trim());
    }

    /**
     * Optional helper:
     * - Agar baad me Flipkart specs table se values nikalni ho
     *   to ye method use kar sakte ho.
     */
    private String pickFirstByLabel(Document doc, String... labels)
    {
        Elements rows = doc.select("table tr"); // selector ko page ke hisab se tweak karna
        for (Element row : rows)
        {
            Elements tds = row.select("td");
            if (tds.size() < 2) continue;

            String labelText = tds.get(0).text().toLowerCase();
            for (String label : labels)
            {
                if (labelText.contains(label.toLowerCase()))
                {
                    return tds.get(1).text();
                }
            }
        }
        return null;
    }
}
