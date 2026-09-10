# 🚌 Public Bus Route Finder — Kathmandu Valley

> **Academic Java project** — Helps passengers find the cheapest or shortest public bus journey
> across Kathmandu, Lalitpur, and Bhaktapur using **Dijkstra's algorithm** on a live transit graph.

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active-brightgreen)]()

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#️-architecture)
- [Quick Start — Web App](#-quick-start--web-app-primary)
- [Quick Start — JavaFX Desktop](#-quick-start--javafx-desktop-legacy)
- [Project Structure](#-project-structure)
- [Tech Stack](#️-tech-stack)
- [Seed Data — Transit Network](#-seed-data--kathmandu-valley-transit-network)
- [API Reference](#-rest-api-reference)
- [Demo Credentials](#-demo-credentials)
- [Changes & Changelog](#-changes--what-was-done-and-why)
- [OOP & Design Principles](#-oop--design-principles)
- [Known Limitations](#-known-limitations)
- [Future Improvements](#-future-improvements)

---

## 🌏 Overview

Citizens in the Kathmandu Valley often don't know which bus (or combination of buses)
gets them from A to B, and what it should cost. This project digitizes that lookup:

- Models the entire valley transit network as a **weighted graph** — stops are nodes, route segments are edges
- Runs **Dijkstra's algorithm** to find the optimal path (by fare or distance), including automatic multi-bus transfers
- Displays a step-by-step itinerary: which bus to board, where to transfer, fare per leg, and totals
- Provides a full **Admin Console** to manage stops, routes, and segments at runtime

### What users can do

| Role | Capabilities |
|------|-------------|
| 🧑 **Guest / Passenger** | Search routes, view all bus lines, view transit map |
| 🔑 **Admin** | Everything above + add/delete stops, create routes, connect segments, reset data |

---

## 🏗️ Architecture

The repository contains **two parallel implementations** that share the `model/` and `service/` packages:

```
┌──────────────────────────────────────────────────────────┐
│                     SHARED CORE                          │
│                                                          │
│  model/BusStop  ·  model/BusRoute  ·  model/RouteSegment │
│  service/RouteFinderService  (Dijkstra's algorithm)      │
│  service/FareCalculatorService                           │
│  util/GraphBuilder  (builds adjacency list)              │
└─────────────────────┬────────────────────┬───────────────┘
                      │                    │
          ┌───────────▼──────┐   ┌─────────▼─────────────┐
          │  WEB APP (Primary)│   │  JAVAFX APP (Legacy)  │
          │                  │   │                       │
          │  server/         │   │  gui/LoginView        │
          │    WebServer     │   │  gui/AdminDashboard   │
          │    InMemory      │   │  gui/PassengerDash    │
          │    DataStore     │   │                       │
          │                  │   │  dao/ (MySQL CRUD)    │
          │  web/            │   │  util/DatabaseConn    │
          │    index.html    │   │  sql/schema.sql       │
          │    app.js        │   │                       │
          │    style.css     │   │  Data: MySQL DB       │
          │    seed-data.json│   │  Auth: BCrypt         │
          │                  │   │                       │
          │  Data: In-memory │   │  Entry: MainLauncher  │
          │  Auth: BCrypt +  │   └───────────────────────┘
          │    UUID tokens   │
          │                  │
          │  Entry: Main.java│
          └──────────────────┘
```

> **`Main.java` launches the Web App.** The JavaFX path (`MainLauncher.java`) is a documented
> alternate mode for running the desktop app with a live MySQL database.

---

## 🚀 Quick Start — Web App (Primary)

**Requirements:** Java 17+ only. No database, no extra setup.

### Option 1 — Double-click (Windows)

Double-click **`run-web.bat`** in File Explorer.

It will automatically compile all Java backend classes and open `http://localhost:8080` in your browser.

### Option 2 — Terminal

```bat
cd "C:\path\to\Public-Bus-Route-Finder-Kathmandu-Valley"
run-web.bat
```

### Option 3 — Manual compile & run

```bat
javac -cp "src;lib/*" -d out/production/Public-Bus-Route-Finder-Kathmandu-Valley ^
  src/model/enums/*.java src/model/*.java src/service/*.java src/util/*.java src/server/*.java

java -cp "out/production/Public-Bus-Route-Finder-Kathmandu-Valley;lib/*" server.WebServer
```

Then visit: **[http://localhost:8080](http://localhost:8080)**

> Press `Ctrl+C` in the terminal to stop the server.

---

## 🖥️ Quick Start — JavaFX Desktop (Legacy)

**Requirements:** Java 17+, JavaFX SDK, MySQL 8+

### 1. Clone the repository

```bash
git clone https://github.com/anush-821/Public-Bus-Route-Finder-Kathmandu-Valley.git
cd Public-Bus-Route-Finder-Kathmandu-Valley
```

### 2. Set up the MySQL database

Open **MySQL Workbench** or the `mysql` CLI and run:

```sql
source sql/schema.sql;
```

This creates the `kathmandu_bus_db` database with tables for stops, routes, segments, and users.

### 3. Configure the database connection

Database credentials are read from **environment variables** (not hardcoded).
Copy the example file and fill in your values:

```bat
copy .env.example .env
```

Edit `.env`:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=kathmandu_bus_db
DB_USER=root
DB_PASS=your_password
```

> ⚠️ `.env` is listed in `.gitignore` — never commit real credentials.

### 4. Add required JARs to `lib/`

| JAR | Download from |
|-----|--------------|
| `mysql-connector-j-x.x.x.jar` | [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) |
| JavaFX SDK JARs | Already bundled in `lib/` |

### 5. Run from IntelliJ IDEA

Open the project → right-click `src/MainLauncher.java` → **Run 'MainLauncher.main()'**

---

## 📁 Project Structure

```
Public-Bus-Route-Finder-Kathmandu-Valley/
│
├── src/
│   ├── Main.java                    ← Entry point → starts WebServer (web app)
│   ├── MainLauncher.java            ← JavaFX entry point (desktop app)
│   │
│   ├── model/                       ← Shared domain entities
│   │   ├── BusStop.java             ← stopId, name, area, lat, lon
│   │   ├── BusRoute.java            ← routeId, name, operator, farePerKm
│   │   ├── RouteSegment.java        ← directed edge: fromStop→toStop, distance, fare
│   │   ├── User.java                ← userId, name, email, bcrypt password, role
│   │   └── enums/
│   │       └── UserRole.java        ← ADMIN | PASSENGER
│   │
│   ├── service/                     ← Shared business logic
│   │   ├── RouteFinderService.java  ← Dijkstra's algorithm (FARE or DISTANCE mode)
│   │   └── FareCalculatorService.java ← Formats NPR & km for display
│   │
│   ├── util/                        ← Shared utilities
│   │   ├── GraphBuilder.java        ← Builds adjacency list, adds reverse edges
│   │   └── DatabaseConnection.java  ← MySQL connection (reads env vars)
│   │
│   ├── server/                      ← Web app backend
│   │   ├── WebServer.java           ← Built-in Java HTTP server, 12 REST endpoints
│   │   └── InMemoryDataStore.java   ← Thread-safe singleton, loads seed-data.json
│   │
│   ├── dao/                         ← JavaFX/MySQL data access layer
│   │   ├── BusStopDAO.java          ← Interface
│   │   ├── BusStopDAOImpl.java      ← MySQL implementation
│   │   ├── BusRouteDAO.java         ← Interface
│   │   ├── BusRouteDAOImpl.java     ← MySQL implementation (transactional deletes)
│   │   ├── UserDAO.java             ← Interface
│   │   └── UserDAOImpl.java         ← MySQL implementation with BCrypt check
│   │
│   └── gui/                         ← JavaFX desktop UI
│       ├── LoginView.java           ← Login screen, routes by role
│       ├── PassengerDashboardView.java ← Stop pickers, route result, itinerary list
│       ├── AdminDashboardView.java  ← 3-tab console: Stops / Routes / Segments
│       └── style.css               ← JavaFX stylesheet
│
├── web/                             ← Frontend (served by WebServer)
│   ├── index.html                  ← Single-page app shell (Leaflet map + 3 panels)
│   ├── app.js                      ← All frontend logic (1,100+ lines, vanilla JS)
│   ├── style.css                   ← Design system: dark/light themes, animations
│   └── seed-data.json              ← 18 stops, 6 routes, 18 segments (Kathmandu Valley)
│
├── sql/
│   └── schema.sql                  ← MySQL schema + sample data for desktop mode
│
├── lib/                            ← Bundled JARs (JavaFX, Gson 2.10.1, jBCrypt 0.4)
├── run-web.bat                     ← One-click launcher for the web app
├── .env.example                    ← Template for database credentials
├── .gitignore
├── LICENSE
└── README.md
```

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Language | **Java 17+** | Core application logic |
| Web Server | **`com.sun.net.httpserver`** | Zero-dependency HTTP server (built into JDK) |
| JSON | **Gson 2.10.1** | REST API serialization / seed-data parsing |
| Password Hashing | **jBCrypt 0.4** | Secure password storage (never plain-text) |
| Map Library | **Leaflet.js 1.9.4** | Interactive transit map in the browser |
| Map Tiles | **OpenStreetMap** | Free map tiles — no API key required |
| Frontend | **Vanilla HTML/CSS/JS** | SPA frontend, no framework needed |
| Desktop GUI | **JavaFX 17+** | Desktop application UI (legacy mode) |
| Database | **MySQL 8+** | Persistence for JavaFX/desktop mode only |
| JDBC | **MySQL Connector/J** | Java–MySQL connectivity (desktop mode) |
| IDE | **IntelliJ IDEA** | Recommended development environment |

---

## 🚏 Seed Data — Kathmandu Valley Transit Network

The web app ships with **18 real bus stops**, **6 routes**, and **18 directed segments**
pre-loaded from `web/seed-data.json`:

### Bus Stops

| ID | Stop | Area |
|----|------|------|
| 1 | Ratna Park | Kathmandu (central hub) |
| 2 | Sundhara | Kathmandu |
| 3 | New Baneshwor | Baneshwor |
| 4 | Koteshwor | Koteshwor (eastern hub) |
| 5 | Tribhuvan Airport | Sinamangal |
| 6 | Kalanki | Kalanki (western hub) |
| 7 | Balkhu | Kirtipur Road |
| 8 | Kalimati | Kalimati |
| 9 | Balaju | Balaju |
| 10 | Gongabu Bus Park | Gongabu (northern hub) |
| 11 | Swayambhu | Swayambhu |
| 12 | Lagankhel | Lalitpur (southern hub) |
| 13 | Patan Dhoka | Lalitpur |
| 14 | Jawalakhel | Lalitpur |
| 15 | Bhaktapur Durbar Square | Bhaktapur |
| 16 | Suryabinayak | Bhaktapur |
| 17 | Chabahil | Chabahil |
| 18 | Boudha | Boudha |

### Bus Routes

| Route | Operator | Fare/km |
|-------|---------|---------|
| Route 1: Ratnapark → Airport → Koteshwor | Sajha Yatayat | NPR 4.5 |
| Route 2: Ratnapark → Kalanki → Balkhu | Nepal Yatayat | NPR 4.0 |
| Route 3: Gongabu → Balaju → Swayambhu → Kalimati | City Bus Service | NPR 4.5 |
| Route 4: Lagankhel → Patan Dhoka → Jawalakhel | Sajha Yatayat | NPR 4.0 |
| Route 5: Koteshwor → Chabahil → Boudha | Boudha Sewa Yatayat | NPR 5.0 |
| Route 6: Ratnapark → Baneshwor → Koteshwor → Bhaktapur | Bhaktapur Yatayat | NPR 5.5 |

> All segments are treated as **bidirectional** — `GraphBuilder` automatically creates
> a reverse edge for every segment, so every route is usable in both directions.

---

## 🔌 REST API Reference

All endpoints are served by `WebServer.java` at `http://localhost:8080`.

| Method | Endpoint | Auth Required | Description |
|--------|----------|:-------------:|-------------|
| `POST` | `/api/login` | — | Authenticate, returns `{ token, role, name }` |
| `POST` | `/api/logout` | Bearer token | Invalidates session token |
| `GET` | `/api/stops` | — | List all bus stops |
| `POST` | `/api/stops` | Admin token | Create a new stop |
| `DELETE` | `/api/stops/{id}` | Admin token | Delete stop (cascades its segments) |
| `GET` | `/api/routes` | — | List all bus routes |
| `POST` | `/api/routes` | Admin token | Create a new route |
| `DELETE` | `/api/routes/{id}` | Admin token | Delete route (cascades its segments) |
| `GET` | `/api/segments` | — | List all route segments |
| `POST` | `/api/segments` | Admin token | Add a segment (graph edge) |
| `POST` | `/api/find-route` | — | Run Dijkstra, returns full journey result |
| `POST` | `/api/reset` | Admin token | Reset all data to seed defaults |

Admin endpoints require the HTTP header:
```
Authorization: Bearer <token-from-login>
```

---

## 🔑 Demo Credentials

| Role | Email | Password |
|------|-------|---------|
| 🏢 Admin | `admin@ktmbus.gov.np` | `admin123` |
| 🧑 Passenger | `passenger@example.com` | `passenger123` |

> Passwords are stored as **BCrypt hashes** — never in plain text.
> These credentials are for demonstration only.

---

## 📝 Changes & What Was Done and Why

This section documents every significant change made to the codebase from the original
version, along with the rationale for each decision.

---

### 1. 🌐 Web App Established as Primary Entry Point
**Files:** `Main.java`, `README.md`

**What changed:** `Main.java` was confirmed to launch `server.WebServer` directly.
`MainLauncher.java` (JavaFX) is retained as a documented alternate/legacy mode.

**Why:** The web app requires zero external dependencies (no MySQL, no JavaFX SDK config),
making it immediately runnable on any machine with Java 17+. It is the more accessible
and demonstrable entry point for an academic project.

---

### 2. 🔐 Server-Side Session Token Authentication
**File:** `src/server/WebServer.java`

**What changed:** Implemented a full session system using random **UUID tokens**.
After a successful login, the server generates a UUID, stores it in a `ConcurrentHashMap`
mapped to the user's role, and returns it to the client. All mutating admin endpoints
(`POST`/`DELETE` for stops, routes, segments) require the `Authorization: Bearer <token>` header
and validate that the token belongs to an `ADMIN` role. Unauthenticated requests receive `401`.

**Why:** The original code had no server-side authorization — any browser could call admin
endpoints directly. This change enforces that only logged-in admins can modify data,
which is a fundamental security requirement.

---

### 3. 🔒 BCrypt Password Hashing
**Files:** `src/server/InMemoryDataStore.java`, `src/dao/UserDAOImpl.java`

**What changed:** All passwords are now stored as **BCrypt hashes** using the
`jBCrypt 0.4` library (`org.mindrot.jbcrypt.BCrypt`). `InMemoryDataStore` hashes
the demo passwords at startup. `UserDAOImpl` uses `BCrypt.checkpw()` for MySQL-mode
login verification.

**Why:** Plain-text or MD5 passwords are a critical vulnerability. BCrypt is the
industry standard for password storage — it is slow by design (making brute-force
attacks impractical) and includes a random salt, preventing rainbow table attacks.

---

### 4. 🌱 Canonical Seed Data (`seed-data.json`)
**File:** `web/seed-data.json`

**What changed:** Created a single JSON file containing 18 real Kathmandu Valley
bus stops (with accurate GPS coordinates), 6 bus routes (with real operators and
fare rates), and 18 route segments forming a connected transit graph. This file is
loaded by `InMemoryDataStore` at startup and is also used by the frontend.

**Why:** Previously, data was scattered in hard-coded Java arrays and duplicated
between the backend and frontend. A single source-of-truth file eliminates duplication,
makes it easy to add new stops/routes, and keeps GPS coordinates accurate.

---

### 5. ⚙️ Environment Variable Database Credentials
**Files:** `src/util/DatabaseConnection.java`, `.env.example`

**What changed:** `DatabaseConnection.java` was refactored to read all MySQL credentials
(`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`) from environment variables via
`System.getenv()` instead of having them hardcoded as string literals. A `.env.example`
template file was added.

**Why:** Hardcoded credentials in source code are a serious security risk — they end up
committed to version control and visible to anyone with repository access. Using environment
variables is the standard approach for keeping secrets out of code.

---

### 6. 🗺️ Map Tile Source Fixed (No API Key Required)
**File:** `web/app.js`

**What changed:** The `updateMapTiles()` function was updated to use **OpenStreetMap**
tile URLs (`tile.openstreetmap.org`) instead of CARTO basemaps. Dark mode is achieved
using a CSS `invert(1) hue-rotate(180deg)` filter on the map container element.

**Why:** CARTO basemaps now require a registered account and API key, causing the map
to show blank tiles for users without a key. OpenStreetMap is completely free, has no
rate limits for normal use, and requires no account or configuration. This makes the
project immediately runnable without any setup.

---

### 7. 🚦 Port Standardized to 8080
**Files:** `src/server/WebServer.java`, `run-web.bat`

**What changed:** The server port was standardized to `8080` across all files. The
`run-web.bat` script and the `WebServer.java` startup message now consistently reference
the same port.

**Why:** An earlier version of the code had a mismatch between the port referenced in
the batch script and the port the server actually listened on, causing "connection
refused" errors after clicking the auto-opened browser tab.

---

### 8. 🧹 Removed Unused Import (`JsonArray` in `WebServer.java`)
**File:** `src/server/WebServer.java` (line 5)

**What changed:** Removed the unused `import com.google.gson.JsonArray;` statement.

**Why:** `JsonArray` was never referenced in `WebServer.java` — all JSON serialization
uses `Gson.toJson()` directly. Unused imports are noise that reduces code clarity and
triggers IDE warnings.

---

### 9. ✅ Input Validation Added to JavaFX Admin Dashboard
**File:** `src/gui/AdminDashboardView.java`

**What changed:** Replaced the old `parseOrZero()` utility calls with proper
`parseDoubleOrNull()` and `parsePositiveDouble()` / `parsePositiveInt()` helper methods
that return `null` on invalid input. The form action handlers now check for `null` returns
and display a descriptive error message in the `statusLabel` without submitting the form.
Latitude is validated to the range `[-90, 90]` and longitude to `[-180, 180]`.

**Why:** The original code silently defaulted invalid numbers to `0`, which could create
stops at the Gulf of Guinea (0°, 0°) or routes with zero fare. Proper validation rejects
bad input immediately with a human-readable message.

---

### 10. 🔄 `deleteRoute` Made Transactional in `BusRouteDAOImpl`
**File:** `src/dao/BusRouteDAOImpl.java`

**What changed:** The `deleteRoute()` method now wraps the segment deletion and route
deletion inside a single JDBC **transaction** (`conn.setAutoCommit(false)` + `commit()`
+ `rollback()` on failure).

**Why:** Without a transaction, if the server crashed between deleting segments and
deleting the route, the database would be left in an inconsistent state — a route with
no segments, or orphaned segments with no parent route. The transaction guarantees both
operations succeed together or neither does.

---

### 11. 📦 Third-Party Libraries Added to `lib/`
**Files:** `lib/gson-2.10.1.jar`, `lib/jbcrypt-0.4.jar`

**What changed:** Two JAR files were downloaded and added to the `lib/` directory:
- **Gson 2.10.1** — for JSON serialization in `WebServer.java` and parsing `seed-data.json`
- **jBCrypt 0.4** — for BCrypt password hashing in `InMemoryDataStore` and `UserDAOImpl`

**Why:** These libraries were referenced in the source code but not present in the
repository, causing compilation failures. Bundling them in `lib/` (which is already
on the IDE classpath via the `.iml` configuration) makes the project compile and run
without any additional setup.

---

## 🧠 OOP & Design Principles

| Principle | Where Applied |
|-----------|--------------|
| **Encapsulation** | All model fields are `private` with getters/setters only (`BusStop`, `BusRoute`, `RouteSegment`, `User`) |
| **Abstraction** | `BusStopDAO`, `BusRouteDAO`, `UserDAO` interfaces hide all SQL/JDBC from the GUI and service layers |
| **Polymorphism** | `BusStopDAOImpl`, `BusRouteDAOImpl`, `UserDAOImpl` are concrete implementations swappable behind their interfaces |
| **Separation of Concerns** | `model` ↔ `dao` ↔ `service` ↔ `gui/server` are strictly layered; services never call DAOs directly in the web mode |
| **Single Responsibility** | `GraphBuilder` only builds the graph; `RouteFinderService` only runs Dijkstra; `FareCalculatorService` only formats output |
| **Thread Safety** | `InMemoryDataStore` uses `ConcurrentHashMap`, `Collections.synchronizedList`, `AtomicInteger`, and `synchronized` methods |

---

## ⚠️ Known Limitations

| Limitation | Details |
|-----------|---------|
| **No persistence** | Web app data resets on every server restart (in-memory only) |
| **Disconnected zones** | Some stops (e.g. Lagankhel/Patan) are isolated from Bhaktapur without a Koteshwor transfer |
| **No timetables** | Bus schedules and frequencies are not modelled |
| **No unit tests** | `RouteFinderService` and `GraphBuilder` have no automated test coverage |
| **Plain HTTP** | Web server runs on HTTP only — acceptable for local/academic use |

---

## 🔮 Future Improvements

- 💾 **Persistent storage** — SQLite or H2 embedded DB so web app data survives restarts
- 🧪 **Unit tests** — JUnit 5 tests for `RouteFinderService`, `GraphBuilder`, and `InMemoryDataStore`
- 🕒 **Timetables** — Model bus schedules and estimate arrival times
- 📱 **Mobile companion** — Android or iOS app using the same REST API
- 🚦 **Traffic-aware routing** — Factor in peak-hour delays
- 💳 **Digital ticketing** — QR-code fare card integration
- 📊 **Ridership analytics** — Admin dashboard with journey counts and popular routes
- 🔒 **HTTPS** — TLS termination via a reverse proxy (Nginx) for production deployment
- 👥 **Multi-operator admin** — Per-operator admin roles, not just one global admin

---

## 🎯 Project Objectives

1. Digitize public bus route discovery for the Kathmandu Valley
2. Apply **graph theory** (Dijkstra's algorithm) to a real-world transit problem
3. Demonstrate a clean **layered OOP architecture** in Java (`model/dao/service/gui`)
4. Practice **JDBC + MySQL** integration with a JavaFX front end
5. Build and serve a **zero-dependency web application** from the Java standard library
6. Apply **security best practices**: BCrypt hashing, session tokens, server-side authorization

---

## 👨‍💻 Academic Project

Developed as a **Java Object-Oriented Programming / Software Development** academic project,
applying graph algorithms, layered application design, and web technologies to a real-world
municipal transit scenario in the Kathmandu Valley, Nepal.

---

*Built with Java · Gson · jBCrypt · JavaFX · Leaflet.js · OpenStreetMap · HTML/CSS/JS*
