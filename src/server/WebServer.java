package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.BusRoute;
import model.BusStop;
import model.RouteSegment;
import model.User;
import model.enums.UserRole;
import service.RouteFinderService;
import util.GraphBuilder;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Lightweight HTTP server for the Public Bus Route Finder
 * Kathmandu Valley web application.
 *
 * Uses Gson for JSON serialization/deserialization, enforces server-side
 * token-based authorization for admin endpoints, and validates numeric inputs.
 */
public class WebServer {

    private static final int PORT = 8080;
    private static final Path WEB_DIR = Paths.get("web").toAbsolutePath();
    private static final InMemoryDataStore dataStore = InMemoryDataStore.getInstance();
    private static final Gson gson = new Gson();

    private static final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    public static class UserSession {
        public final String token;
        public final int userId;
        public final String fullName;
        public final String email;
        public final UserRole role;

        public UserSession(String token, User user) {
            this.token = token;
            this.userId = user.getUserId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.role = user.getRole();
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newCachedThreadPool());

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
    // Auth Verification Helper
    // =========================================================================
    private static UserSession getSession(HttpExchange exchange) {
        List<String> headers = exchange.getRequestHeaders().get("Authorization");
        if (headers != null && !headers.isEmpty()) {
            String authHeader = headers.get(0);
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                return activeSessions.get(token);
            }
        }
        return null;
    }

    private static boolean requireAdminAuth(HttpExchange exchange) throws IOException {
        UserSession session = getSession(exchange);
        if (session == null || session.role != UserRole.ADMIN) {
            JsonObject err = new JsonObject();
            err.addProperty("error", "Unauthorized: Admin privileges required");
            sendJsonResponse(exchange, 401, gson.toJson(err));
            return false;
        }
        return true;
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
                sendJsonResponse(exchange, 200, gson.toJson(stops));
                return;
            }

            if ("POST".equals(method)) {
                if (!requireAdminAuth(exchange)) return;

                String body = readBody(exchange);
                JsonObject json = parseJsonObject(body);

                String name = json.has("name") ? json.get("name").getAsString().trim() : "";
                String area = json.has("area") ? json.get("area").getAsString().trim() : "";

                if (name.isEmpty() || area.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Stop name and area are required\"}");
                    return;
                }

                Double lat = getDoubleOrNull(json, "latitude");
                Double lon = getDoubleOrNull(json, "longitude");

                if (lat == null || lat < -90.0 || lat > 90.0) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Latitude must be a valid number between -90 and 90\"}");
                    return;
                }
                if (lon == null || lon < -180.0 || lon > 180.0) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Longitude must be a valid number between -180 and 180\"}");
                    return;
                }

                BusStop created = dataStore.createStop(name, area, lat, lon);
                sendJsonResponse(exchange, 201, gson.toJson(created));
                return;
            }

            if ("DELETE".equals(method)) {
                if (!requireAdminAuth(exchange)) return;

                String query = exchange.getRequestURI().getQuery();
                int id = -1;
                if (query != null && query.contains("id=")) {
                    id = parseIntSafe(query.split("id=")[1].split("&")[0], -1);
                } else {
                    String body = readBody(exchange);
                    JsonObject json = parseJsonObject(body);
                    if (json.has("id")) {
                        id = json.get("id").getAsInt();
                    }
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
                sendJsonResponse(exchange, 200, gson.toJson(routes));
                return;
            }

            if ("POST".equals(method)) {
                if (!requireAdminAuth(exchange)) return;

                String body = readBody(exchange);
                JsonObject json = parseJsonObject(body);
                String routeName = json.has("routeName") ? json.get("routeName").getAsString().trim() : "";
                String operatorName = json.has("operatorName") ? json.get("operatorName").getAsString().trim() : "";

                if (routeName.isEmpty() || operatorName.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Route name and operator name are required\"}");
                    return;
                }

                Double farePerKm = getDoubleOrNull(json, "farePerKm");
                if (farePerKm == null || farePerKm <= 0) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Fare per km must be a positive number\"}");
                    return;
                }

                BusRoute created = dataStore.createRoute(routeName, operatorName, farePerKm);
                sendJsonResponse(exchange, 201, gson.toJson(created));
                return;
            }

            if ("DELETE".equals(method)) {
                if (!requireAdminAuth(exchange)) return;

                String query = exchange.getRequestURI().getQuery();
                int id = -1;
                if (query != null && query.contains("id=")) {
                    id = parseIntSafe(query.split("id=")[1].split("&")[0], -1);
                } else {
                    String body = readBody(exchange);
                    JsonObject json = parseJsonObject(body);
                    if (json.has("id")) {
                        id = json.get("id").getAsInt();
                    }
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
                sendJsonResponse(exchange, 200, gson.toJson(segments));
                return;
            }

            if ("POST".equals(method)) {
                if (!requireAdminAuth(exchange)) return;

                String body = readBody(exchange);
                JsonObject json = parseJsonObject(body);

                Integer routeId = getIntOrNull(json, "routeId");
                Integer fromStopId = getIntOrNull(json, "fromStopId");
                Integer toStopId = getIntOrNull(json, "toStopId");
                Double distanceKm = getDoubleOrNull(json, "distanceKm");
                Double fareNpr = getDoubleOrNull(json, "fareNpr");
                Integer sequenceOrder = getIntOrNull(json, "sequenceOrder");

                if (routeId == null || routeId <= 0) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Valid routeId is required\"}");
                    return;
                }
                if (fromStopId == null || fromStopId <= 0 || toStopId == null || toStopId <= 0 || fromStopId.equals(toStopId)) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Valid distinct fromStopId and toStopId are required\"}");
                    return;
                }
                if (distanceKm == null || distanceKm <= 0) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Distance must be a positive number\"}");
                    return;
                }
                if (fareNpr == null || fareNpr <= 0) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Fare must be a positive number\"}");
                    return;
                }
                if (sequenceOrder == null || sequenceOrder <= 0) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Sequence order must be a positive integer\"}");
                    return;
                }

                BusRoute r = dataStore.getRouteById(routeId);
                String routeName = r != null ? r.getRouteName() : "Route " + routeId;

                RouteSegment seg = new RouteSegment(routeId, routeName, fromStopId, toStopId, distanceKm, fareNpr, sequenceOrder);
                dataStore.addSegment(seg);
                sendJsonResponse(exchange, 201, gson.toJson(seg));
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
            JsonObject json = parseJsonObject(body);

            Integer startStopId = getIntOrNull(json, "fromStopId");
            Integer destStopId = getIntOrNull(json, "toStopId");
            String optimize = json.has("optimizeBy") ? json.get("optimizeBy").getAsString().toUpperCase() : "FARE";

            if (startStopId == null || startStopId <= 0 || destStopId == null || destStopId <= 0) {
                sendJsonResponse(exchange, 400, "{\"error\":\"fromStopId and toStopId are required positive integers\"}");
                return;
            }

            RouteFinderService.OptimizeBy opt = "DISTANCE".equals(optimize)
                    ? RouteFinderService.OptimizeBy.DISTANCE
                    : RouteFinderService.OptimizeBy.FARE;

            List<RouteSegment> allSegments = dataStore.getAllSegments();
            Map<Integer, List<RouteSegment>> graph = GraphBuilder.buildAdjacencyList(allSegments);
            RouteFinderService finder = new RouteFinderService(graph);

            RouteFinderService.JourneyResult result = finder.findBestJourney(startStopId, destStopId, opt);

            if (result == null) {
                sendJsonResponse(exchange, 200, "{\"found\":false,\"message\":\"No transit route found between the selected stops.\"}");
                return;
            }

            int busChanges = finder.countBusChanges(result.getSegments());

            JsonObject resp = new JsonObject();
            resp.addProperty("found", true);
            resp.addProperty("totalFare", result.getTotalFare());
            resp.addProperty("totalDistanceKm", result.getTotalDistanceKm());
            resp.addProperty("busChanges", busChanges);

            // Stop Path
            JsonArray stopPathArray = new JsonArray();
            for (int id : result.getStopPath()) {
                BusStop s = dataStore.getStopById(id);
                if (s != null) {
                    stopPathArray.add(gson.toJsonTree(s));
                } else {
                    JsonObject fallback = new JsonObject();
                    fallback.addProperty("stopId", id);
                    fallback.addProperty("name", "Stop #" + id);
                    stopPathArray.add(fallback);
                }
            }
            resp.add("stopPath", stopPathArray);

            // Segments Path
            JsonArray segmentArray = new JsonArray();
            for (RouteSegment seg : result.getSegments()) {
                BusStop from = dataStore.getStopById(seg.getFromStopId());
                BusStop to = dataStore.getStopById(seg.getToStopId());
                BusRoute route = dataStore.getRouteById(seg.getRouteId());

                JsonObject segObj = new JsonObject();
                segObj.addProperty("routeId", seg.getRouteId());
                segObj.addProperty("routeName", seg.getRouteName());
                segObj.addProperty("operatorName", route != null ? route.getOperatorName() : "Public Bus");
                segObj.addProperty("fromStopId", seg.getFromStopId());
                segObj.addProperty("fromStopName", from != null ? from.getName() : "Stop #" + seg.getFromStopId());
                segObj.addProperty("toStopId", seg.getToStopId());
                segObj.addProperty("toStopName", to != null ? to.getName() : "Stop #" + seg.getToStopId());
                segObj.addProperty("distanceKm", seg.getDistanceKm());
                segObj.addProperty("fareNpr", seg.getFareNpr());
                segObj.addProperty("sequenceOrder", seg.getSequenceOrder());

                segmentArray.add(segObj);
            }
            resp.add("segments", segmentArray);

            sendJsonResponse(exchange, 200, gson.toJson(resp));
        }
    }

    // =========================================================================
    // API: Auth Handler (Issues Session Tokens)
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
            JsonObject json = parseJsonObject(body);
            String email = json.has("email") ? json.get("email").getAsString().trim() : "";
            String password = json.has("password") ? json.get("password").getAsString().trim() : "";

            User user = dataStore.authenticate(email, password);
            if (user != null) {
                String token = UUID.randomUUID().toString();
                UserSession session = new UserSession(token, user);
                activeSessions.put(token, session);

                JsonObject resp = new JsonObject();
                resp.addProperty("success", true);
                resp.addProperty("token", token);

                JsonObject uObj = new JsonObject();
                uObj.addProperty("userId", user.getUserId());
                uObj.addProperty("fullName", user.getFullName());
                uObj.addProperty("email", user.getEmail());
                uObj.addProperty("role", user.getRole().name());
                resp.add("user", uObj);

                sendJsonResponse(exchange, 200, gson.toJson(resp));
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
                if (!requireAdminAuth(exchange)) return;

                dataStore.resetToDefault();
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Transit database reset to Kathmandu Valley defaults.\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // =========================================================================
    // Helpers: Input Parsing & Validation
    // =========================================================================
    private static JsonObject parseJsonObject(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return new JsonObject();
        }
        try {
            return gson.fromJson(jsonStr, JsonObject.class);
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private static Double getDoubleOrNull(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) return null;
        try {
            return json.get(key).getAsDouble();
        } catch (Exception e) {
            try {
                return Double.parseDouble(json.get(key).getAsString().trim());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static Integer getIntOrNull(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) return null;
        try {
            return json.get(key).getAsInt();
        } catch (Exception e) {
            try {
                return Integer.parseInt(json.get(key).getAsString().trim());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static int parseIntSafe(String s, int defaultVal) {
        if (s == null) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defaultVal;
        }
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
}
