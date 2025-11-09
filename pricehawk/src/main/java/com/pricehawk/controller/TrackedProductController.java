package com.pricehawk.controller;

import com.pricehawk.entity.TrackedProduct;
import com.pricehawk.service.TrackedProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🌐 REST Controller for tracking products.
 *
 * 🧩 Endpoints:
 *  - POST /api/tracked → Add new model to tracking list
 *  - GET /api/tracked → View all tracked models
 */

@RestController
@RequestMapping("/api/tracked")
public class TrackedProductController
{

    @Autowired
    private TrackedProductService trackedProductService;

    // ➕ Add product to tracking list
    @PostMapping
    public TrackedProduct addTrackedProduct(@RequestBody TrackedProduct product)
    {
        return trackedProductService.addTrackedProduct(product);
    }

    // 📋 View all tracked products
    @GetMapping
    public List<TrackedProduct> getAllTrackedProducts()
    {
        return trackedProductService.getAllTrackedProducts();
    }
}
