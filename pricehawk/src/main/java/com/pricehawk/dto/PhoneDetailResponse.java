package com.pricehawk.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/*
 * Data coming from external phone API (GSMArena etc.)
 * Keeping it slightly flexible because API structure is not fully consistent.
 */
@Data
public class PhoneDetailResponse
{

    private String name;
    private String releaseDate;
    private String displaySize;
    private String chipset;
    private String battery;

    // API sometimes returns specs in grouped/variable format
    // so keeping it generic instead of strict model for now
    private List<Map<String, Object>> specifications;

    // main product image
    private String img;
}