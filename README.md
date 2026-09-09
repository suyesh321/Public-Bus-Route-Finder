# 🚌 Public Bus Route Finder — Kathmandu Valley (Web Edition & Desktop App)

A modern **Web Application & Java backend** that helps passengers find the
cheapest or shortest route between any two bus stops in the Kathmandu Valley
(Kathmandu, Lalitpur, Bhaktapur), featuring an interactive **Leaflet.js transit map**,
**Dijkstra's algorithm**, multi-bus transfer detection, fare calculations, and a full
**Admin Management Console**.

Now available as a responsive **Web Application** (served via the zero-dependency
built-in Java `WebServer` or running standalone in any modern browser) alongside
the original JavaFX desktop application.

---

## 🚀 Quick Start (Web Application)

### Option 1: Run with Java Backend Server
Double click `run-web.bat` or execute in your terminal:
```bash
# Compile and run the Java WebServer
javac -cp "src;lib/*" -d out/production/Public-Bus-Route-Finder-Kathmandu-Valley src/model/enums/*.java src/model/*.java src/service/*.java src/util/*.java src/server/*.java
java -cp "out/production/Public-Bus-Route-Finder-Kathmandu-Valley;lib/*" server.WebServer
```
Then open: **[http://localhost:8080](http://localhost:8080)**

### Option 2: Run Standalone Web App (No setup required)
Simply open `web/index.html` in Google Chrome, Microsoft Edge, or Firefox! The client-side transit engine and interactive Kathmandu Valley map will run immediately with pre-bundled transit data.

---

## 📌 Overview

Citizens in the Kathmandu Valley are often unsure which bus (or combination
of buses) gets them from A to B, and what it should cost. This app digitizes
that lookup:

- Store the valley's bus **stops** and **routes** in a MySQL database.
- Model the transit network as a **graph** (stops = nodes, route segments =
  edges).
- Use **Dijkstra's algorithm** to compute the cheapest or shortest journey,
  automatically handling transfers between routes.
- Show a step-by-step itinerary: which bus to board, where to get off,
  where to transfer, and the total fare/distance.

### 👤 Passenger side
- Register / log in, or continue as a guest
- Pick a "From" stop and a "To" stop
- Choose to optimize by **cheapest fare** or **shortest distance**
- See a turn-by-turn itinerary with per-leg fare and distance, and the total

### 🏢 Admin side
- Add / delete bus stops
- Create new bus routes and operators
- Add ordered route segments (stop-to-stop legs) that make up each route

---

## ✨ Key Features

- 🔎 **Graph-based route finding** — `service.RouteFinderService` builds a
  weighted graph from all route segments and runs Dijkstra's algorithm to
  find the optimal path, including multi-bus transfers.
- 💰 **Two optimization modes** — cheapest fare (NPR) or shortest distance (km).
- 🔁 **Automatic bidirectional segments** — a road served by a route is
  treated as usable in both directions without duplicate data entry.
- 🧭 **Bus change detection** — the app tells the passenger how many buses
  they need to board for a given journey.
- 🛠️ **Admin console** — manage stops, routes, and segments without touching
  the database directly.
- 🧾 **Layered architecture** — `model` / `dao` / `service` / `gui` / `util`
  separation, same pattern used in the reference municipality project.

---

## 🧠 OOP & Software Design

- **Encapsulation** — `BusStop`, `BusRoute`, `RouteSegment`, `User` expose
  private fields via getters/setters only.
- **Abstraction** — `BusStopDAO`, `BusRouteDAO`, `UserDAO` interfaces hide
  JDBC/SQL details from the GUI and service layers.
- **Polymorphism** — `BusStopDAOImpl`, `BusRouteDAOImpl`, `UserDAOImpl`
  provide concrete implementations of the DAO interfaces.
- **Separation of concerns**:

```
gui      → JavaFX screens (LoginView, PassengerDashboardView, AdminDashboardView)
dao      → JDBC database access
model    → Data entities (BusStop, BusRoute, RouteSegment, User)
service  → Business logic (RouteFinderService, FareCalculatorService)
util     → DatabaseConnection, GraphBuilder
```

---

## 🏗️ Project Architecture

```
┌─────────────────────────┐
│        JavaFX UI        │
│ Passenger / Admin views │
└────────────┬─────────────┘
             │
             ▼
┌─────────────────────────┐
│         Services         │
│ RouteFinderService        │
│ (Dijkstra's algorithm)    │
│ FareCalculatorService     │
└────────────┬─────────────┘
             │
             ▼
┌─────────────────────────┐
│           DAO             │
│ BusStopDAO / BusRouteDAO  │
│ UserDAO                   │
└────────────┬─────────────┘
             │
             ▼
┌─────────────────────────┐
│          MySQL            │
│      kathmandu_bus_db     │
└─────────────────────────┘
```

---

## 📁 Project Structure

```
BusRouteFinder-KathmanduValley/
│
├── src/
│   ├── dao/
│   │   ├── BusStopDAO.java
│   │   ├── BusStopDAOImpl.java
│   │   ├── BusRouteDAO.java
│   │   ├── BusRouteDAOImpl.java
│   │   ├── UserDAO.java
│   │   └── UserDAOImpl.java
│   │
│   ├── gui/
│   │   ├── LoginView.java
│   │   ├── PassengerDashboardView.java
│   │   ├── AdminDashboardView.java
│   │   └── style.css
│   │
│   ├── model/
│   │   ├── BusStop.java
│   │   ├── BusRoute.java
│   │   ├── RouteSegment.java
│   │   ├── User.java
│   │   └── enums/
│   │       └── UserRole.java
│   │
│   ├── service/
│   │   ├── RouteFinderService.java
│   │   └── FareCalculatorService.java
│   │
│   ├── util/
│   │   ├── DatabaseConnection.java
│   │   └── GraphBuilder.java
│   │
│   ├── Main.java
│   └── MainLauncher.java
│
├── sql/
│   └── schema.sql
│
├── lib/                 (place mysql-connector-j and javafx jars here)
├── .gitignore
└── README.md
```

---

## 🛠️ Tech Stack

| Technology            | Purpose                          |
| ---------------------- | --------------------------------- |
| ☕ Java 17+             | Core programming language         |
| 🎨 JavaFX 17+ (or 21)  | Desktop GUI                       |
| 🗄️ MySQL 8+            | Database                          |
| 🔌 MySQL Connector/J   | Java–MySQL connectivity (JDBC)    |
| 💻 IntelliJ IDEA / Eclipse / VS Code | Development environment |

---

## 💻 What You Need Installed on Your Desktop

To open, edit, and run this project you'll need the following installed
locally:

1. **Java Development Kit (JDK) 17 or newer**
   - Download: https://adoptium.net (Eclipse Temurin) or Oracle JDK
   - Verify with `java -version` and `javac -version` in a terminal.

2. **JavaFX SDK** (only needed separately if your JDK doesn't bundle it —
   most modern JDKs from Adoptium/Oracle do not include JavaFX by default)
   - Download: https://gluonhq.com/products/javafx/
   - Note the path to the `lib` folder inside the SDK; you'll pass it as a
     VM option (`--module-path` / `--add-modules`) when running.

3. **MySQL Community Server 8.x**
   - Download: https://dev.mysql.com/downloads/mysql/
   - Also install **MySQL Workbench** (bundled with the installer on
     Windows/macOS) for a GUI way to run `sql/schema.sql`.

4. **MySQL Connector/J** (JDBC driver JAR)
   - Download: https://dev.mysql.com/downloads/connector/j/
   - Place the `.jar` file in the project's `lib/` folder, or add it as a
     dependency if you use Maven/Gradle.

5. **An IDE with JavaFX + Maven/Gradle support** (pick one):
   - **IntelliJ IDEA** (Community or Ultimate) — recommended
   - **Eclipse IDE for Java Developers** + e(fx)clipse plugin
   - **VS Code** + "Extension Pack for Java" + "JavaFX Support" extensions

6. **Git** (to clone/manage the repository)
   - Download: https://git-scm.com/downloads

7. *(Optional but recommended)* **Maven** or **Gradle**, if you want to
   manage the JavaFX and MySQL Connector/J dependencies automatically
   instead of manually downloading JARs into `lib/`.

### Quick tool checklist

| Tool | Required? | Notes |
| --- | --- | --- |
| JDK 17+ | ✅ Required | Compiles and runs the app |
| JavaFX SDK | ✅ Required | Powers the GUI (unless your JDK bundles it) |
| MySQL Server 8+ | ✅ Required | Stores stops/routes/users |
| MySQL Workbench | Recommended | Easiest way to run `schema.sql` |
| MySQL Connector/J | ✅ Required | JDBC driver, put in `lib/` |
| IntelliJ IDEA / Eclipse / VS Code | ✅ Required (pick one) | IDE to build & run |
| Git | Recommended | Version control |
| Maven or Gradle | Optional | Simplifies dependency management |

---

## 🚀 Getting Started

### 1️⃣ Clone or download the project

```
git clone https://github.com/anush-821/Public-Bus-Route-Finder-Kathmandu-Valley.git
cd BusRouteFinder-KathmanduValley
```

### 2️⃣ Set up the database

Open **MySQL Workbench** (or the `mysql` CLI) and run:

```
sql/schema.sql
```

This creates the `kathmandu_bus_db` database with sample Kathmandu Valley
stops, routes, and demo accounts.

### 3️⃣ Configure the database connection

Open:

```
src/util/DatabaseConnection.java
```

Update your MySQL credentials:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/kathmandu_bus_db?useSSL=false&serverTimezone=UTC";

private static final String USER = "root";

private static final String PASSWORD = "your_password";
```

⚠️ Do not commit real database passwords to GitHub. For a real deployment,
use environment variables or a config file excluded via `.gitignore`.

### 4️⃣ Add the required libraries

Place these JAR files on your classpath (e.g. inside `lib/`, or configure
them as dependencies in your IDE / build tool):

- `mysql-connector-j-<version>.jar`
- JavaFX SDK `lib/` folder (if not bundled with your JDK)

### 5️⃣ Run the application

Open the project in **IntelliJ IDEA** (or your IDE of choice), open:

```
src/MainLauncher.java
```

Then **Right Click → Run 'MainLauncher.main()'**.

If running from the command line instead, something like:

```
javac -d out --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls src/**/*.java
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls -cp "out;lib/mysql-connector-j-8.x.x.jar" MainLauncher
```
(use `:` instead of `;` between classpath entries on macOS/Linux)

---

## 🔑 Demo Credentials

| Role       | Email                       | Password        |
| ---------- | ---------------------------- | ---------------- |
| 🏢 Admin    | `admin@ktmbus.gov.np`        | `admin123`       |
| 👤 Passenger | `passenger@example.com`     | `passenger123`   |
| 👤 Passenger | Register through UI, or "Continue as Guest" | Chosen at registration |

> ⚠️ These credentials are for demonstration/development purposes only.

---

## 🔐 Security Considerations

This is an academic/portfolio project. For production use, add:

- Password hashing (e.g. BCrypt) instead of plain-text passwords
- Environment-based database credentials
- Input validation and sanitization
- Prepared statements everywhere (already used throughout the DAO layer)
- Session management and role-based authorization
- HTTPS/API security if converted to a web architecture

---

## 🔮 Future Improvements

- 🗺️ Live map view of stops and the chosen route (e.g. embedded map widget)
- 📱 Companion mobile app
- 🕒 Real-time bus arrival estimates
- 🚦 Traffic-aware travel time estimation (not just distance/fare)
- 💳 Digital ticketing / fare card integration
- 🌐 Web-based version
- 🧑‍💼 Multiple admin roles (per-operator admins)
- 📊 Ridership analytics dashboard

---

## 🎯 Project Objectives

1. Digitize public bus route discovery for the Kathmandu Valley.
2. Apply graph theory (Dijkstra's algorithm) to a real-world transit problem.
3. Demonstrate layered, OOP-based desktop application design in Java.
4. Practice JDBC + MySQL integration with a JavaFX front end.
5. Provide a foundation that could be extended toward a real civic tool.

---

## 👨‍💻 Development Focus

```
Java
│
├── Object-Oriented Programming
├── JavaFX GUI Development
├── Graph Algorithms (Dijkstra's shortest path)
├── JDBC
├── MySQL Database Management
├── DAO Architecture
├── Business Logic Separation
└── Authentication
```

---

## Academic Project

This project was developed as a Java Object-Oriented Programming / Software
Development project, applying graph algorithms and layered application
design to a real-world municipal transit scenario in the Kathmandu Valley.

---

**Built with Java + MySQL + HTML + Javascript + CSS**
