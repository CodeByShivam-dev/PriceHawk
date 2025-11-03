# 🦅 PriceHawk

### 🚀 Real-Time Smartphone Price Comparison & Notification System

**PriceHawk** is a powerful, production-grade Spring Boot application designed to fetch and compare real-time smartphone prices from multiple e-commerce platforms.  
It also provides price-drop alerts and intelligent API execution with multithreading for optimal performance.

---

## 🧱 Architecture Overview

```text
client (web/mobile)
    ↓
REST Controller  →  Service Layer  →  Repository (DB)
                     ↓
                Concurrency Engine
                     ↓
                Twilio OTP Service


| Layer                    | Technology                           |
| :----------------------- | :----------------------------------- |
| **Backend Framework**    | Spring Boot                          |
| **Language**             | Java                                 |
| **Build Tool**           | Maven                                |
| **Database**             | PostgreSQL                           |
| **API Integration**      | REST API (Amazon, Flipkart, etc.)    |
| **Concurrency Handling** | ExecutorService / ThreadPoolExecutor |
| **Notification**         | Twilio (WhatsApp / SMS OTP)          |
| **Version Control**      | Git & GitHub                         |


💡 Key Features

🔍 Smart Price Comparison – Fetches real-time smartphone prices from multiple stores.

⚡ High-Performance Async Engine – Manages multiple API calls efficiently using a custom thread pool.

🧠 Intelligent Request Queueing – Handles API overloads gracefully.

🔔 Price-Drop Notifications – Sends instant alerts via WhatsApp/SMS using Twilio.

🧾 Clean REST Architecture – Controller → Service → Repository structure.

🧰 Scalable Design – Built to handle real-world concurrency and load.



src/
 └── main/
      ├── java/com/pricehawk/
      │    ├── config/        → Async & App Configurations
      │    ├── controller/    → REST Controllers
      │    ├── service/       → Business Logic Layer
      │    ├── dto/           → Data Transfer Objects
      │    ├── model/         → Entity Classes
      │    └── repository/    → Database Access Layer
      └── resources/
           ├── application.yml
           └── static / templates (if required)

