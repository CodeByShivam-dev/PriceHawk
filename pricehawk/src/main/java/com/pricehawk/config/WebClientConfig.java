package com.pricehawk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig
{

    @Bean
    public WebClient webClient()
    {
        return WebClient.builder()
                // Base endpoint for external specs API
                .baseUrl("https://gsmarena-api.vercel.app/api")
                // Basic identifier to avoid generic bot blocking
                .defaultHeader("User-Agent", "PriceHawk/1.0")
                .build();
    }
}
