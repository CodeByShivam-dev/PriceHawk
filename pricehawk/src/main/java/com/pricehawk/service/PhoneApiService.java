package com.pricehawk.service;

import com.pricehawk.dto.PhoneDetailResponse;
import com.pricehawk.model.SearchResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Thin client around the GSMArena API.
 *
 * Keeps all external API interaction in one place so controllers
 * and business services remain independent from HTTP details.
 */
@Service
public class PhoneApiService
{

    private final WebClient webClient;

    public PhoneApiService(WebClient webClient)
    {
        this.webClient = webClient;
    }

    /**
     * Searches devices by name and returns matching candidates.
     *
     * Example:
     * "iPhone 15" -> list of matching device records.
     */
    public SearchResponse[] searchPhone(String name)
    {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", name)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse[].class)
                .block();
    }

    /**
     * Fetches the detailed specification payload for a device.
     *
     * The slug is typically obtained from the search endpoint.
     */
    public PhoneDetailResponse getPhoneDetails(String slug)
    {
        return webClient.get()
                .uri("/device/" + slug)
                .retrieve()
                .bodyToMono(PhoneDetailResponse.class)
                .block();
    }
}