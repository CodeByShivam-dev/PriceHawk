# ⚡ PriceHawk — AI-Powered Smartphone Price Intelligence Engine

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-red?logo=java&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-green?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-Live%20DB-blue?logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Jsoup-Web%20Scraping-orange" alt="Jsoup"/>
  <img src="https://img.shields.io/badge/ExecutorService-Async%20Engine-purple" alt="ExecutorService"/>
  <img src="https://img.shields.io/badge/Maven-Build-yellow" alt="Maven"/>
</p>

<p align="center">
  <b>🔍 Smart Search · 💰 Real-Time Pricing · 🧠 Intelligent Decisions</b><br>
  A high-performance backend system that aggregates, analyzes, and enriches smartphone pricing data across major Indian e-commerce platforms.
</p>

---

## 🧠 Vision

PriceHawk is engineered to solve a real-world problem:

> 👉 **"Finding the best smartphone price across fragmented e-commerce platforms in real time."**

It acts as a **distributed price intelligence engine** that:

- ✅ Scrapes live pricing data
- ✅ Normalizes product models
- ✅ Caches results intelligently
- ✅ Enriches data with specifications
- ✅ Tracks historical pricing trends

---

## 🚨 Problem Statement

E-commerce pricing is:

| Challenge | Impact |
|-----------|--------|
| **Fragmented** | Prices vary across Amazon, Flipkart, Croma |
| **Inconsistent** | Same model has different prices |
| **Manual Comparison** | Users waste time switching between sites |
| **Rapid Changes** | Prices change during sales/festives |

Users waste time switching between:
- 🛒 Amazon
- 🛒 Flipkart
- 🛒 Croma

> 👉 **PriceHawk eliminates this friction** by aggregating everything into a unified response layer.

---

## ⚙️ System Architecture

```text
                        ┌────────────────────────────┐
                        │        Frontend UI         │
                        │ (HTML / JS / Bootstrap)    │
                        └─────────────┬──────────────┘
                                      │ REST API
                                      ▼
                        ┌────────────────────────────┐
                        │ SmartphoneController       │
                        │ (API Gateway Layer)        │
                        └─────────────┬──────────────┘
                                      │
                                      ▼
                ┌────────────────────────────────────────┐
                │ SmartphoneService (CORE ORCHESTRATOR)  │
                │        ⭐ MAIN BRAIN ENGINE             │
                └──────────────┬─────────────┬───────────┘
                               │             │
               ┌───────────────▼─┐       ┌───▼────────────────┐
               │ Cache Layer      │       │ Async Execution    │
               │ PriceSnapshot DB │       │ ExecutorService    │
               │ (3h reuse window)│       │ Parallel Scraping  │
               └───────────────┬─┘       └───┬────────────────┘
                               │             │
                               └──────┬──────┘
                                      ▼
                      ┌────────────────────────────────┐
                      │ PriceScraperService            │
                      │ (Multi-Vendor Aggregator)      │
                      └──────┬──────────┬──────────────┘
                             │          │
         ┌────────────────────▼┐  ┌────▼────────────────┐  ┌────────────────┐
         │ Amazon Scraper      │  │ Flipkart Scraper    │  │ Croma Scraper   │
         │ (Jsoup DOM parsing) │  │ (Fallback selectors)│  │ (Resilient)     │
         └─────────┬──────────┘  └────────┬────────────┘  └──────┬─────────┘
                   │                       │                       │
                   └──────────────┬────────┴──────────────┬──────┘
                                  ▼
                    ┌───────────────────────────────┐
                    │ SmartphonePriceResult DTO     │
                    │ (Unified Response Model)      │
                    └──────────────┬────────────────┘
                                   ▼
        ┌────────────────────────────────────────────────────┐
        │ Specs Engine (Jsoup + Extractors + DB Cache)       │
        │                                                    │
        │  Amazon / Flipkart / Generic Spec Extractors       │
        │  ↓                                                  │
        │  PhoneSpecs DB Cache (Normalized Lookup)           │
        └───────────────────┬────────────────────────────────┘
                            ▼
                ┌──────────────────────────────┐
                │ Enriched Product Results      │
                │ (Price + Specs + Rating)      │
                └──────────────┬───────────────┘
                               ▼
                ┌──────────────────────────────┐
                │ PhoneSpecsService            │
                │ (Async Background Worker)    │
                └──────────────┬───────────────┘
                               ▼
                ┌──────────────────────────────────────────┐
                │ Persistence Layer                        │
                │                                          │
                │  • PriceSnapshot (Price History)         │
                │  • SearchHistory (Analytics)             │
                │  • PhoneSpecs (Specs Cache)              │
                └───────────────────┬──────────────────────┘
                                    ▼
                        ┌──────────────────────┐
                        │ Frontend Response    │
                        │ Sorted Best Deals    │
                        │ Store Redirect Links  │
                        └──────────────────────┘
```
## 🔥 Key Features

### ⚡ Real-Time Multi-Platform Price Aggregation
- Amazon, Flipkart, Croma scraping
- Parallel execution using `ExecutorService`

### 🧠 Intelligent Caching Layer
- Normalized model-based caching
- 3-hour snapshot reuse window

### 🔍 Robust Web Scraping Engine
- Jsoup-based resilient selectors
- Multi-fallback DOM parsing strategy

### 📊 Price History Tracking
- Append-only `PriceSnapshot` model
- Enables future trend prediction

### 🧩 Async Specification Enrichment
- Non-blocking background scraping
- Separate worker thread pool

### 📦 Unified DTO Pipeline
- `SmartphonePriceResult` standard output format

### 🔔 Future-Ready Notification System
- Price drop alerts (architecture already prepared)

---

## 🧱 Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17 + Spring Boot |
| **Scraping** | Jsoup |
| **Concurrency** | ExecutorService / CompletableFuture |
| **Database** | PostgreSQL |
| **API Client** | WebClient |
| **Build Tool** | Maven |
| **Architecture** | Layered + Modular Monolith |

---

## 🧩 Database Schema (Overview)

### 📌 `phone_specs`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key |
| `model_normalized` | VARCHAR | Indexed model key |
| `summary` | TEXT | Specs summary |
| `last_updated` | TIMESTAMP | Refresh time |

### 📌 `price_snapshot`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key |
| `model_normalized` | VARCHAR | Normalized model |
| `store` | VARCHAR | Amazon / Flipkart |
| `price` | DOUBLE | Captured price |
| `product_url` | TEXT | Product link |
| `image_url` | TEXT | Cached image |
| `rating` | DOUBLE | Product rating |
| `in_stock` | BOOLEAN | Availability |
| `captured_at` | TIMESTAMP | Snapshot time |

### 📌 `search_history`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key |
| `query` | TEXT | User search |
| `query_normalized` | TEXT | Indexed key |
| `results_count` | INT | Matches found |
| `searched_at` | TIMESTAMP | Time |

### 📌 `tracked_products`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key |
| `product_name` | TEXT | Product |
| `store` | VARCHAR | Platform |
| `current_price` | DOUBLE | Latest price |
| `target_price` | DOUBLE | Alert threshold |
| `product_url` | TEXT | Link |
| `image_url` | TEXT | Thumbnail |
| `tracked_at` | TIMESTAMP | Start time |

---

## 🔄 System Flow
User Search → Controller
↓
SmartphoneService
↓
Parallel Scraping (Amazon + Flipkart)
↓
DTO Normalization
↓
Cache Check (DB)
↓
Specs Enrichment (Async)
↓
Response Sorted by Price
---

## 📡 API Overview

### 🔎 Search Smartphones

```bash
GET /api/search?query=iphone%2015
```

### 📦 Response

```json
[
  {
    "store": "Amazon",
    "title": "iPhone 15 128GB",
    "price": 72999,
    "productUrl": "...",
    "imageUrl": "...",
    "rating": 4.5,
    "specsSummary": "8GB RAM · 128GB Storage · A16 Bionic"
  }
]
```

### 📊 Sample Output Flow

| Input | Output |
|-------|--------|
| **Samsung S23** | Amazon → ₹69,999 |
| | Flipkart → ₹67,999 ✅ **Best Deal** |
| | Croma → ₹70,499 |

> 👉 System auto-selects best deal + attaches specs

---

## 🚀 Roadmap

- ☐ Redis caching layer for ultra-fast lookup
- ☐ AI-based price prediction engine
- ☐ Notification system (WhatsApp / Email)
- ☐ User authentication system
- ☐ GraphQL API layer
- ☐ React dashboard frontend
- ☐ Mobile app integration

---

## 🧠 Learnings / Design Highlights

- ✅ Built a fault-tolerant scraping architecture
- ✅ Implemented concurrent API aggregation
- ✅ Designed normalized caching strategy
- ✅ Handled unstable HTML structures safely
- ✅ Built async enrichment pipeline
- ✅ Separated concerns across clean service layers

---

## 💼 Skills Demonstrated

| Category | Skills |
|----------|--------|
| **Backend** | Java Backend Engineering, Spring Boot Architecture Design |
| **Scraping** | Web Scraping (Jsoup), DOM Parsing |
| **Concurrency** | Multi-threading & Concurrency, ExecutorService |
| **API** | REST API Development |
| **Database** | Database Design (Normalization), PostgreSQL |
| **System Design** | Scalable Monolith, Performance Optimization |
| **Engineering** | Fault-Tolerant Engineering |

---

## 👨‍💻 Author

**Shivam Kumar**  
Backend Developer | Java | Spring Boot | System Design Enthusiast

---

<p align="center">
  <b>⚡ Built with passion for price intelligence in India 🇮🇳</b>
</p>
