package com.pricehawk.service;

import com.pricehawk.entity.TrackedProduct;
import com.pricehawk.repository.TrackedProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 💼 TrackedProductService
 *
 * 🔹 Handles business logic for adding and listing tracked models.
 * 🔹 Later, scheduler will use this service to check which phones need monitoring.
 */

@Service
public class TrackedProductService
{

    @Autowired
    private TrackedProductRepository trackedProductRepository;

    // ➕ Add new product to tracking list
    public TrackedProduct addTrackedProduct(TrackedProduct product) {
        return trackedProductRepository.save(product);
    }

    // 📋 View all tracked products
    public List<TrackedProduct> getAllTrackedProducts() {
        return trackedProductRepository.findAll();
    }
}
