package com.pricehawk.service;

import com.pricehawk.entity.TrackedProduct;
import com.pricehawk.repository.TrackedProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 📌 TrackedProductService
 *
 * This service acts as a lightweight domain layer around tracked products.
 * The idea is simple: users can “follow” specific smartphone models, and
 * my scheduled price-monitoring engine will periodically check these models
 * to detect price drops or stock changes.
 *
 * Why this class matters in the overall PriceHawk workflow:
 * --------------------------------------------------------
 * 1️⃣ Search Layer → User finds a product
 * 2️⃣ User clicks "Track Price" → POST to this service
 * 3️⃣ Scheduler picks tracked items → hits the scrapers
 * 4️⃣ If price drops → Notification service fires (WhatsApp/SMS/Email)
 *
 * This class keeps the tracking subsystem decoupled:
 * - No scraping logic here
 * - No notification logic here
 * - Only persistence + domain handling
 *
 * This separation ensures clean responsibilities and testable modules.
 */

@Service
public class TrackedProductService
{
    @Autowired
    private TrackedProductRepository trackedProductRepository;

    /**
     * ➕ Add a new product to the user’s personal tracking list.
     *
     * Why save directly?
     * - Product validation (duplicates, broken URLs, inactive models)
     *   can be added later as a pre-check layer.
     * - Keeping this method simple prevents over-coupling with scraper logic.
     *
     * @param product The product model & user mapping that needs tracking.
     * @return Saves and returns the persisted model, including DB-generated ID.
     */
    public TrackedProduct addTrackedProduct(TrackedProduct product)
    {
        return trackedProductRepository.save(product);
    }

    /**
     * 📄 Fetch ALL tracked products in the system.
     *
     * Used mainly for:
     * - Admin dashboards
     * - Debugging the scheduler
     * - Bulk operations (like clearing orphan entries)
     *
     * Not used by end users directly, but extremely useful in operations.
     */
    public List<TrackedProduct> getAllTrackedProducts()
    {
        return trackedProductRepository.findAll();
    }

    /**
     * 👤 Fetch tracked products for one specific user.
     *
     * Why separate this method?
     * - Avoid exposing global tracked list to regular users.
     * - Enables personalized tracking dashboards.
     * - This directly supports "user-specific notifications".
     *
     * @param userId The unique user identifier.
     * @return List of all models the user wants us to monitor.
     */
    public List<TrackedProduct> getTrackedProductsByUser(Long userId)
    {
        return trackedProductRepository.findByUserId(userId);
    }

    /**
     * 📡 (Future Extension)
     * getProductsEligibleForNotification()
     *
     * This method will later allow:
     * - Scheduler to fetch only items where lastChecked < now - X minutes
     * - Filtering models that recently had a price change
     * - Attaching a "notification cooldown" to prevent spam
     *
     * This placeholder is intentional to show clear future expansion points.
     */
}
