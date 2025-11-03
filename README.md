<h1 align="center">⚡ PriceHawk — AI-Powered Smartphone Price Comparator</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-red?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-green?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-Live%20DB-blue?logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/ExecutorService-Concurrency%20Engine-orange?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Build-Maven-yellow?logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-lightgrey" />
</p>

<p align="center">
  <strong>Smart Search. Smarter Decisions.</strong><br>
  PriceHawk doesn’t just compare — it predicts smarter buying decisions.
</p>

---

## 🧠 Vision

**PriceHawk** is a real-time **smartphone price comparison system** built using **Spring Boot + Java Concurrency**, designed to fetch, compare, and analyze smartphone prices across platforms like **Amazon, Flipkart, and Croma** using APIs — enhanced with AI-driven recommendations and price trend notifications.

> The goal: Deliver the best price, every time — with real-time updates, concurrency safety, and scalable design.

---

## 🚀 Current Progress

✅ **Thread Pool Executor (Async Engine)**  
→ Handles multiple concurrent API requests efficiently  
→ Uses custom `AsyncConfig` with `ThreadPoolExecutor`  
→ Prevents overload using queue capacity and fallback policy  

✅ **Smartphone Controller (API Layer)**  
→ Accepts smartphone search queries  
→ Fetches live data from multiple sources (mock stage)  
→ Integrates with service layer for price aggregation  

✅ **PostgreSQL Integration (Base Ready)**  
→ Configurable in `application.properties`  
→ Future-ready for storing historical price trends  

---

## ⚙️ Tech Stack

| Layer | Technology |
|:--|:--|
| Language | **Java (17+)** |
| Framework | **Spring Boot 3.5.7** |
| Database | **PostgreSQL (runtime)** |
| Async / Multithreading | **ExecutorService (ThreadPool)** |
| Exception Handling | **Custom + Global Handler (planned)** |
| Build Tool | **Maven** |
| Future AI Layer | **Price Drop Prediction (ML)** |

---

## 🧩 System Design (Phase 1)

- **User Query:** Search any smartphone model  
- **API Aggregation:** Calls multiple e-commerce APIs concurrently  
- **Result Merge:** Sorts and filters the best deals  
- **Price Alerts:** Email or SMS notification planned  
- **Data Persistence:** Logs queries and user preferences for insights  

---

## 🔜 Roadmap

- [ ] Integrate live APIs (Amazon, Flipkart, Croma)  
- [ ] Redis caching for faster repeated searches  
- [ ] Notification system (email/SMS for price drops)  
- [ ] Global Exception Handling  
- [ ] AI-based price prediction module  
- [ ] Frontend dashboard (React or Thymeleaf)  

---

## 🧱 Architecture Overview

```text
client (web/mobile)
    ↓
REST Controller  →  Service Layer  →  Repository (DB)
                     ↓
                Concurrency Engine
                     ↓
                API Aggregator (Amazon / Flipkart / Croma)

About

Author: Shivam Kumar
Goal: Build an AI-backed smartphone comparison engine with concurrency, notifications, and intelligent insights.
Status: 🚧 In development (Core API + Async Engine ready)



