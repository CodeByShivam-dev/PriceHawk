package com.pricehawk.model;  // ← YE package

import lombok.Data;

@Data
public class SearchResponse {
    private String name;
    private String slug;
    private String img;
}
