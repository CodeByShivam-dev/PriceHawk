package com.pricehawk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Welcome to PriceHawk Backend 🚀";
    }

    @GetMapping("/api/ping")
    public String ping() {
        return "✅ PriceHawk API is up and running!";
    }
}
