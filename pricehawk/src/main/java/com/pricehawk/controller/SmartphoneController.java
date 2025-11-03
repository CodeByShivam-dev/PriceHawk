package com.pricehawk.controller;

import com.pricehawk.dto.SmartphoneResponse;
import com.pricehawk.service.SmartphoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🌐 REST Controller for handling smartphone price comparison requests.
 *
 * 🧩 Responsibilities:
 *  - Expose endpoints for frontend (React/Vercel, etc.)
 *  - Receive search queries like "iPhone 15"
 *  - Delegate processing to the SmartphoneService
 *  - Return real-time price comparison results in JSON
 *
 * ⚙️ Example Endpoint:
 *      GET /api/smartphones?query=iPhone+15
 *
 * 📦 Response Example:
 *      [
 *          {
 *              "store": "Amazon",
 *              "price": 74999,
 *              "rating": 4.5,
 *              "inStock": true
 *          }
 *      ]
 */

@RestController
@RequestMapping("/api/smartphones")
public class SmartphoneController {

    @Autowired
    private SmartphoneService smartphoneService;

    /**
     * 🔍 Search smartphones across multiple stores (Amazon, Flipkart, etc.)
     *
     * @param query The smartphone name entered by the user.
     * @return List of best price options with AI verdict.
     */
    @GetMapping
    public List<Map<String, Object>> searchSmartphones(@RequestParam String query)
    {
        return smartphoneService.fetchSmartphoneData(query);
    }
}
