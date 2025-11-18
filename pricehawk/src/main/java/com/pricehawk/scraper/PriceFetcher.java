package com.pricehawk.scraper;

import com.pricehawk.dto.SmartphonePriceResult;
import java.util.List;

public interface PriceFetcher
{
    List<SmartphonePriceResult> fetchPrices(String query);
}
