package com.pricehawk.scraper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class RandomImageProvider
{
    private static final List<String> FALLBACK_IMAGES = List.of(
            "https://via.placeholder.com/300.png?text=Smartphone",
            "https://via.placeholder.com/300.png?text=Mobile+Device",
            "https://via.placeholder.com/300.png?text=Phone",
            "https://via.placeholder.com/300.png?text=Android+Phone",
            "https://via.placeholder.com/300.png?text=5G+Phone"
    );

    private static final Random RANDOM = new Random();

    /**
     * Returns an image URL for a phone.
     * If phone name is available → context-aware placeholder
     * Else → random fallback image
     */
    public static String getImage(String phoneName)
    {
        if (phoneName == null || phoneName.trim().isEmpty())
        {
            return getRandomFallbackImage();
        }

        return "https://via.placeholder.com/300.png?text=" +
                URLEncoder.encode(phoneName, StandardCharsets.UTF_8);
    }

    private static String getRandomFallbackImage()
    {
        return FALLBACK_IMAGES.get(RANDOM.nextInt(FALLBACK_IMAGES.size()));
    }
}
