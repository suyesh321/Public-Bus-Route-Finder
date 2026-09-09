package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.BusRoute;
import model.BusStop;
import model.RouteSegment;
import model.User;
import service.RouteFinderService;
import util.GraphBuilder;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Lightweight, zero-dependency HTTP server for the Public Bus Route Finder
 * Kathmandu Valley web application.
 *
 * Serves the responsive frontend web application from the 'web/' directory
 * and exposes REST API endpoints for transit data, Dijkstra pathfinding,
 * and admin management.
 */
public class WebServer {

    private static final int PORT = 8082;
    private static final Path WEB_DIR = Paths.get("web").toAbsolutePath();
    private static final InMemoryDataStore dataStore = InMemoryDataStore.getInstance();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor() != null ?
                Executors.newCachedThreadPool() : Executors.newCachedThreadPool());

        // Static files handler
        server.createContext("/", new StaticFileHandler());

        // API endpoints
        server.createContext("/api/stops", new StopsHandler());
        server.createContext("/api/routes", new RoutesHandler());
        server.createContext("/api/segments", new SegmentsHandler());
        server.createContext("/api/find-route", new FindRouteHandler());
        server.createContext("/api/auth/login", new AuthHandler());
        server.createContext("/api/reset", new ResetHandler());

        server.start();
        System.out.println("===============================================================");
        System.out.println(" 🚌 Public Bus Route Finder — Kathmandu Valley (Web Edition)");
        System.out.println(" Server running at: http://localhost:" + PORT);
        System.out.println(" Serving frontend from: " + WEB_DIR);
        System.out.println(" Press Ctrl+C to stop.");
        System.out.println("===============================================================");
    }

    // =========================================================================
    // Static File Handler
    // =========================================================================
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Prevent path traversal
            Path resolved = WEB_DIR.resolve(path.substring(1)).normalize();
            if (!resolved.startsWith(WEB_DIR) || !Files.exists(resolved) || Files.isDirectory(resolved)) {
                resolved = WEB_DIR.resolve("index.html");
            }

            if (!Files.exists(resolved)) {
                String notFound = "404 Not Found - web directory or index.html missing.";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String contentType = getMimeType(resolved.toString());
            byte[] bytes = Files.readAllBytes(resolved);

            sendCors(exchange);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // =========================================================================
    // API: Bus Stops Handler
    // =========================================================================
    static class StopsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);
            String method = exchange.getRequestMethod().toUpperCase();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(method)) {
                List<BusStop> stops = dataStore.getAllStops();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < stops.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(stopToJson(stops.get(i)));
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
                return;
            }

            if ("POST".equals(method)) {
                String body = readBody(exchange);
                Map<String, String> json = parseSimpleJson(body);
                String name = json.getOrDefault("name", "").trim();
                String area = json.getOrDefault("area", "").trim();
                double lat = parseDoubleSafe(json.get("latitude"), 27.7000);
                double lon = parseDoubleSafe(json.get("longitude"), 85.3200);

                if (name.isEmpty() || area.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Stop name and area are required\"}");
                    return;
                }

                BusStop created = dataStore.createStop(name, area, lat, lon);
                sendJsonResponse(exchange, 201, stopToJson(created));
                return;
            }

            if ("DELETE".equals(method)) {
                String query = exchange.getRequestURI().getQuery();
                int id = -1;
                if (query != null && query.contains("id=")) {
                    id = parseIntSafe(query.split("id=")[1].split("&")[0], -1);
                } else {
                    String body = readBody(exchange);
                    Map<String, String> json = parseSimpleJson(body);
                    id = parseIntSafe(json.get("id"), -1);
                }

                if (id != -1 && dataStore.deleteStop(id)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Stop deleted\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Stop not found\"}");
                }
                return;
            }

            exchange.sendResponseHeaders(405, -1);
        }
    }

    // =========================================================================
    // API: Bus Routes Handler
    // =========================================================================
    static class RoutesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);
            String method = exchange.getRequestMethod().toUpperCase();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(method)) {
                List<BusRoute> routes = dataStore.getAllRoutes();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < routes.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(routeToJson(routes.get(i)));
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
                return;
            }

            if ("POST".equals(method)) {
                String body = readBody(exchange);
                Map<String, String> json = parseSimpleJson(body);
                String routeName = json.getOrDefault("routeName", "").trim();
                String operatorName = json.getOrDefault("operatorName", "").trim();
                double farePerKm = parseDoubleSafe(json.get("farePerKm"), 5.0);

                if (routeName.isEmpty() || operatorName.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Route name and operator name are required\"}");
                    return;
                }

                BusRoute created = dataStore.createRoute(routeName, operatorName, farePerKm);
                sendJsonResponse(exchange, 201, routeToJson(created));
                return;
            }

            if ("DELETE".equals(method)) {
                String query = exchange.getRequestURI().getQuery();
                int id = -1;
                if (query != null && query.contains("id=")) {
                    id = parseIntSafe(query.split("id=")[1].split("&")[0], -1);
                } else {
                    String body = readBody(exchange);
                    Map<String, String> json = parseSimpleJson(body);
                    id = parseIntSafe(json.get("id"), -1);
                }

                if (id != -1 && dataStore.deleteRoute(id)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Route deleted\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Route not found\"}");
                }
                return;
            }

            exchange.sendResponseHeaders(405, -1);
        }
    }

    // =========================================================================
    // API: Route Segments Handler
    // =========================================================================
    static class SegmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);
            String method = exchange.getRequestMethod().toUpperCase();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(method)) {
                List<RouteSegment> segments = dataStore.getAllSegments();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < segments.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(segmentToJson(segments.get(i)));
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
                return;
            }

            if ("POST".equals(method)) {
                String body = readBody(exchange);
                Map<String, String> json = parseSimpleJson(body);
                int routeId = parseIntSafe(json.get("routeId"), -1);
                int fromStopId = parseIntSafe(json.get("fromStopId"), -1);
                int toStopId = parseIntSafe(json.get("toStopId"), -1);
                double distanceKm = parseDoubleSafe(json.get("distanceKm"), 1.0);
                double fareNpr = parseDoubleSafe(json.get("fareNpr"), 15.0);
                int seq = parseIntSafe(json.get("sequenceOrder"), 1);

                BusRoute r = dataStore.getRouteById(routeId);
                String routeName = r != null ? r.getRouteName() : "Route " + routeId;

                if (fromStopId <= 0 || toStopId <= 0 || fromStopId == toStopId) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Valid distinct from and to stops are required\"}");
                    return;
                }

                RouteSegment seg = new RouteSegment(routeId, routeName, fromStopId, toStopId, distanceKm, fareNpr, seq);
                dataStore.addSegment(seg);
                sendJsonResponse(exchange, 201, segmentToJson(seg));
                return;
            }

            exchange.sendResponseHeaders(405, -1);
        }
    }

    // =========================================================================
    // API: Find Route (Dijkstra Pathfinding)
    // =========================================================================
    static class FindRouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = readBody(exchange);
            Map<String, String> json = parseSimpleJson(body);

            int startStopId = parseIntSafe(json.get("fromStopId"), -1);
            int destStopId = parseIntSafe(json.get("toStopId"), -1);
            String optimize = json.getOrDefault("optimizeBy", "FARE").toUpperCase();

            RouteFinderService.OptimizeBy opt = "DISTANCE".equals(optimize)
                    ? RouteFinderService.OptimizeBy.DISTANCE
                    : RouteFinderService.OptimizeBy.FARE;

            if (startStopId <= 0 || destStopId <= 0) {
                sendJsonResponse(exchange, 400, "{\"error\":\"fromStopId and toStopId are required\"}");
                return;
            }

            List<RouteSegment> allSegments = dataStore.getAllSegments();
            Map<Integer, List<RouteSegment>> graph = GraphBuilder.buildAdjacencyList(allSegments);
            RouteFinderService finder = new RouteFinderService(graph);

            RouteFinderService.JourneyResult result = finder.findBestJourney(startStopId, destStopId, opt);

            if (result == null) {
                sendJsonResponse(exchange, 200, "{\"found\":false,\"message\":\"No transit route found between the selected stops.\"}");
                return;
            }

            int busChanges = finder.countBusChanges(result.getSegments());

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"found\":true,");
            sb.append("\"totalFare\":").append(result.getTotalFare()).append(",");
            sb.append("\"totalDistanceKm\":").append(result.getTotalDistanceKm()).append(",");
            sb.append("\"busChanges\":").append(busChanges).append(",");

            // Stop Path
            sb.append("\"stopPath\":[");
            List<Integer> stopIds = result.getStopPath();
            for (int i = 0; i < stopIds.size(); i++) {
                if (i > 0) sb.append(",");
                BusStop s = dataStore.getStopById(stopIds.get(i));
                if (s != null) {
                    sb.append(stopToJson(s));
                } else {
                    sb.append("{\"stopId\":").append(stopIds.get(i)).append(",\"name\":\"Stop #").append(stopIds.get(i)).append("\"}");
                }
            }
            sb.append("],");

            // Edge / Segment Path
            sb.append("\"segments\":[");
            List<RouteSegment> segs = result.getSegments();
            for (int i = 0; i < segs.size(); i++) {
                if (i > 0) sb.append(",");
                RouteSegment seg = segs.get(i);
                BusStop from = dataStore.getStopById(seg.getFromStopId());
                BusStop to = dataStore.getStopById(seg.getToStopId());
                BusRoute route = dataStore.getRouteById(seg.getRouteId());

                sb.append("{");
                sb.append("\"routeId\":").append(seg.getRouteId()).append(",");
                sb.append("\"routeName\":\"").append(escapeJson(seg.getRouteName())).append("\",");
                sb.append("\"operatorName\":\"").append(escapeJson(route != null ? route.getOperatorName() : "Public Bus")).append("\",");
                sb.append("\"fromStopId\":").append(seg.getFromStopId()).append(",");
                sb.append("\"fromStopName\":\"").append(escapeJson(from != null ? from.getName() : "Stop #" + seg.getFromStopId())).append("\",");
                sb.append("\"toStopId\":").append(seg.getToStopId()).append(",");
                sb.append("\"toStopName\":\"").append(escapeJson(to != null ? to.getName() : "Stop #" + seg.getToStopId())).append("\",");
                sb.append("\"distanceKm\":").append(seg.getDistanceKm()).append(",");
                sb.append("\"fareNpr\":").append(seg.getFareNpr()).append(",");
                sb.append("\"sequenceOrder\":").append(seg.getSequenceOrder());
                sb.append("}");
            }
            sb.append("]");

            sb.append("}");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // =========================================================================
    // API: Auth Handler
    // =========================================================================
    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = readBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            String email = json.getOrDefault("email", "").trim();
            String password = json.getOrDefault("password", "").trim();

            User user = dataStore.authenticate(email, password);
            if (user != null) {
                String resp = String.format(
                        "{\"success\":true,\"user\":{\"userId\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}}",
                        user.getUserId(), escapeJson(user.getFullName()), escapeJson(user.getEmail()), user.getRole().name()
                );
                sendJsonResponse(exchange, 200, resp);
            } else {
                sendJsonResponse(exchange, 401, "{\"success\":false,\"error\":\"Invalid email or password. Use admin@ktmbus.gov.np / admin123 or passenger@example.com / passenger123\"}");
            }
        }
    }

    // =========================================================================
    // API: Reset Dataset Handler
    // =========================================================================
    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                dataStore.resetToDefault();
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Transit database reset to Kathmandu Valley defaults.\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // =========================================================================
    // Helpers: JSON Serialization & Parsing
    // =========================================================================
    private static String stopToJson(BusStop s) {
        return String.format(
                "{\"stopId\":%d,\"name\":\"%s\",\"area\":\"%s\",\"latitude\":%.6f,\"longitude\":%.6f}",
                s.getStopId(), escapeJson(s.getName()), escapeJson(s.getArea()), s.getLatitude(), s.getLongitude()
        );
    }

    private static String routeToJson(BusRoute r) {
        return String.format(
                "{\"routeId\":%d,\"routeName\":\"%s\",\"operatorName\":\"%s\",\"farePerKm\":%.2f}",
                r.getRouteId(), escapeJson(r.getRouteName()), escapeJson(r.getOperatorName()), r.getFarePerKm()
        );
    }

    private static String segmentToJson(RouteSegment s) {
        return String.format(
                "{\"routeId\":%d,\"routeName\":\"%s\",\"fromStopId\":%d,\"toStopId\":%d,\"distanceKm\":%.2f,\"fareNpr\":%.2f,\"sequenceOrder\":%d}",
                s.getRouteId(), escapeJson(s.getRouteName()), s.getFromStopId(), s.getToStopId(), s.getDistanceKm(), s.getFareNpr(), s.getSequenceOrder()
        );
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[\"']?([a-zA-Z0-9_]+)[\"']?\\s*:\\s*(?:\"([^\"]*)\"|'([^']*)'|([^,}\\s]+))");
        java.util.regex.Matcher m = p.matcher(json);
        while (m.find()) {
            String key = m.group(1);
            String val = m.group(2) != null ? m.group(2) : (m.group(3) != null ? m.group(3) : m.group(4));
            if (val != null) {
                map.put(key.trim(), val.trim());
            }
        }
        return map;
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int r;
            while ((r = is.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String getMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }

    private static int parseIntSafe(String s, int defaultVal) {
        if (s == null) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static double parseDoubleSafe(String s, double defaultVal) {
        if (s == null) return defaultVal;
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
