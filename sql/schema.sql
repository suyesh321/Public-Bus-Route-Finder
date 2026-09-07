-- ============================================================
-- Public Bus Route Finder - Kathmandu Valley
-- Database schema + sample seed data
-- ============================================================

CREATE DATABASE IF NOT EXISTS kathmandu_bus_db;
USE kathmandu_bus_db;

-- ---------------------------------------------
-- Users (passengers + admin)
-- ---------------------------------------------
DROP TABLE IF EXISTS route_segments;
DROP TABLE IF EXISTS bus_routes;
DROP TABLE IF EXISTS bus_stops;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    role        ENUM('ADMIN', 'PASSENGER') NOT NULL DEFAULT 'PASSENGER'
);

-- ---------------------------------------------
-- Bus stops around the valley
-- ---------------------------------------------
CREATE TABLE bus_stops (
    stop_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    area        VARCHAR(100) NOT NULL,
    latitude    DOUBLE DEFAULT 0,
    longitude   DOUBLE DEFAULT 0
);

-- ---------------------------------------------
-- Bus routes (operators / services)
-- ---------------------------------------------
CREATE TABLE bus_routes (
    route_id      INT AUTO_INCREMENT PRIMARY KEY,
    route_name    VARCHAR(150) NOT NULL,
    operator_name VARCHAR(100) NOT NULL,
    fare_per_km   DOUBLE DEFAULT 5.0
);

-- ---------------------------------------------
-- Ordered segments (edges) that make up each route
-- ---------------------------------------------
CREATE TABLE route_segments (
    segment_id      INT AUTO_INCREMENT PRIMARY KEY,
    route_id        INT NOT NULL,
    from_stop_id    INT NOT NULL,
    to_stop_id      INT NOT NULL,
    distance_km     DOUBLE NOT NULL,
    fare_npr        DOUBLE NOT NULL,
    sequence_order  INT NOT NULL,
    FOREIGN KEY (route_id) REFERENCES bus_routes(route_id) ON DELETE CASCADE,
    FOREIGN KEY (from_stop_id) REFERENCES bus_stops(stop_id),
    FOREIGN KEY (to_stop_id) REFERENCES bus_stops(stop_id)
);

-- ---------------------------------------------
-- Seed: demo accounts
-- ---------------------------------------------
INSERT INTO users (full_name, email, password, role) VALUES
('Valley Transit Admin', 'admin@ktmbus.gov.np', 'admin123', 'ADMIN'),
('Sample Passenger', 'passenger@example.com', 'passenger123', 'PASSENGER');

-- ---------------------------------------------
-- Seed: bus stops (approximate real Kathmandu Valley locations)
-- ---------------------------------------------
INSERT INTO bus_stops (name, area, latitude, longitude) VALUES
('Ratna Park', 'Kathmandu', 27.7040, 85.3140),
('Sundhara', 'Kathmandu', 27.6988, 85.3122),
('New Baneshwor', 'Baneshwor', 27.6928, 85.3405),
('Koteshwor', 'Koteshwor', 27.6776, 85.3486),
('Tribhuvan Airport', 'Sinamangal', 27.6966, 85.3591),
('Kalanki', 'Kalanki', 27.6939, 85.2809),
('Balkhu', 'Kirtipur Road', 27.6858, 85.2966),
('Kalimati', 'Kalimati', 27.6971, 85.3018),
('Balaju', 'Balaju', 27.7280, 85.3050),
('Gongabu Bus Park', 'Gongabu', 27.7326, 85.3193),
('Swayambhu', 'Swayambhu', 27.7150, 85.2903),
('Lagankhel', 'Lalitpur', 27.6667, 85.3235),
('Patan Dhoka', 'Lalitpur', 27.6792, 85.3200),
('Jawalakhel', 'Lalitpur', 27.6742, 85.3126),
('Bhaktapur Durbar Square', 'Bhaktapur', 27.6710, 85.4285),
('Suryabinayak', 'Bhaktapur', 27.6656, 85.4436),
('Chabahil', 'Chabahil', 27.7175, 85.3459),
('Boudha', 'Boudha', 27.7215, 85.3620);

-- ---------------------------------------------
-- Seed: bus routes
-- ---------------------------------------------
INSERT INTO bus_routes (route_name, operator_name, fare_per_km) VALUES
('Route 1: Ratnapark - Airport - Koteshwor', 'Sajha Yatayat', 4.5),
('Route 2: Ratnapark - Kalanki - Balkhu', 'Nepal Yatayat', 4.0),
('Route 3: Gongabu - Balaju - Swayambhu - Kalimati', 'City Bus Service', 4.5),
('Route 4: Lagankhel - Patan Dhoka - Jawalakhel', 'Sajha Yatayat', 4.0),
('Route 5: Koteshwor - Chabahil - Boudha', 'Boudha Sewa Yatayat', 5.0),
('Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur', 'Bhaktapur Yatayat', 5.5);

-- ---------------------------------------------
-- Seed: route segments
-- Using named subqueries to look up stop_id by name keeps this file
-- readable and safe to re-run after a fresh schema load.
-- ---------------------------------------------

-- Route 1: Ratna Park -> Sundhara -> New Baneshwor -> Airport -> Koteshwor
INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance_km, fare_npr, sequence_order)
SELECT r.route_id, s1.stop_id, s2.stop_id, d, f, o FROM
(SELECT 1 AS route_id) r,
(SELECT 'Ratna Park' AS a, 'Sundhara' AS b, 1.2 AS d, 15 AS f, 1 AS o
 UNION ALL SELECT 'Sundhara', 'New Baneshwor', 3.0, 15, 2
 UNION ALL SELECT 'New Baneshwor', 'Tribhuvan Airport', 2.5, 15, 3
 UNION ALL SELECT 'Tribhuvan Airport', 'Koteshwor', 2.0, 15, 4) t
JOIN bus_stops s1 ON s1.name = t.a
JOIN bus_stops s2 ON s2.name = t.b;

-- Route 2: Ratna Park -> Kalimati -> Kalanki -> Balkhu
INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance_km, fare_npr, sequence_order)
SELECT 2, s1.stop_id, s2.stop_id, d, f, o FROM
(SELECT 'Ratna Park' AS a, 'Kalimati' AS b, 2.8 AS d, 15 AS f, 1 AS o
 UNION ALL SELECT 'Kalimati', 'Kalanki', 2.6, 15, 2
 UNION ALL SELECT 'Kalanki', 'Balkhu', 1.8, 15, 3) t
JOIN bus_stops s1 ON s1.name = t.a
JOIN bus_stops s2 ON s2.name = t.b;

-- Route 3: Gongabu -> Balaju -> Swayambhu -> Kalimati
INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance_km, fare_npr, sequence_order)
SELECT 3, s1.stop_id, s2.stop_id, d, f, o FROM
(SELECT 'Gongabu Bus Park' AS a, 'Balaju' AS b, 1.5 AS d, 15 AS f, 1 AS o
 UNION ALL SELECT 'Balaju', 'Swayambhu', 2.2, 15, 2
 UNION ALL SELECT 'Swayambhu', 'Kalimati', 2.4, 15, 3) t
JOIN bus_stops s1 ON s1.name = t.a
JOIN bus_stops s2 ON s2.name = t.b;

-- Route 4: Lagankhel -> Patan Dhoka -> Jawalakhel
INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance_km, fare_npr, sequence_order)
SELECT 4, s1.stop_id, s2.stop_id, d, f, o FROM
(SELECT 'Lagankhel' AS a, 'Patan Dhoka' AS b, 1.6 AS d, 15 AS f, 1 AS o
 UNION ALL SELECT 'Patan Dhoka', 'Jawalakhel', 1.4, 15, 2) t
JOIN bus_stops s1 ON s1.name = t.a
JOIN bus_stops s2 ON s2.name = t.b;

-- Route 5: Koteshwor -> Chabahil -> Boudha
INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance_km, fare_npr, sequence_order)
SELECT 5, s1.stop_id, s2.stop_id, d, f, o FROM
(SELECT 'Koteshwor' AS a, 'Chabahil' AS b, 3.2 AS d, 20 AS f, 1 AS o
 UNION ALL SELECT 'Chabahil', 'Boudha', 1.8, 15, 2) t
JOIN bus_stops s1 ON s1.name = t.a
JOIN bus_stops s2 ON s2.name = t.b;

-- Route 6: Ratna Park -> New Baneshwor -> Koteshwor -> Suryabinayak -> Bhaktapur Durbar Square
INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance_km, fare_npr, sequence_order)
SELECT 6, s1.stop_id, s2.stop_id, d, f, o FROM
(SELECT 'Ratna Park' AS a, 'New Baneshwor' AS b, 3.5 AS d, 20 AS f, 1 AS o
 UNION ALL SELECT 'New Baneshwor', 'Koteshwor', 1.6, 15, 2
 UNION ALL SELECT 'Koteshwor', 'Suryabinayak', 9.0, 35, 3
 UNION ALL SELECT 'Suryabinayak', 'Bhaktapur Durbar Square', 2.0, 15, 4) t
JOIN bus_stops s1 ON s1.name = t.a
JOIN bus_stops s2 ON s2.name = t.b;
