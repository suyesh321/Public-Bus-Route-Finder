package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.BusRoute;
import model.BusStop;
import model.RouteSegment;
import model.User;
import model.enums.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory repository initialized from canonical web/seed-data.json.
 * Uses BCrypt password hashing for authenticating demo & user accounts.
 */
public class InMemoryDataStore {

    private static final InMemoryDataStore INSTANCE = new InMemoryDataStore();

    public static InMemoryDataStore getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, BusStop> stops = new ConcurrentHashMap<>();
    private final Map<Integer, BusRoute> routes = new ConcurrentHashMap<>();
    private final List<RouteSegment> segments = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, User> users = new ConcurrentHashMap<>();

    private final AtomicInteger stopIdGen = new AtomicInteger(100);
    private final AtomicInteger routeIdGen = new AtomicInteger(100);

    private InMemoryDataStore() {
        initDefaultData();
    }

    public synchronized void resetToDefault() {
        stops.clear();
        routes.clear();
        segments.clear();
        users.clear();
        initDefaultData();
    }

    private void initDefaultData() {
        // Users authenticated with BCrypt password hashes
        users.put("admin@ktmbus.gov.np", new User(1, "Valley Transit Admin", "admin@ktmbus.gov.np",
                BCrypt.hashpw("admin123", BCrypt.gensalt()), UserRole.ADMIN));
        users.put("passenger@example.com", new User(2, "Sample Passenger", "passenger@example.com",
                BCrypt.hashpw("passenger123", BCrypt.gensalt()), UserRole.PASSENGER));

        // Load seed data from canonical web/seed-data.json
        Path seedPath = Paths.get("web", "seed-data.json");
        if (Files.exists(seedPath)) {
            try (FileReader reader = new FileReader(seedPath.toFile(), StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                JsonObject seedObj = gson.fromJson(reader, JsonObject.class);

                if (seedObj.has("stops")) {
                    JsonArray stopsArray = seedObj.getAsJsonArray("stops");
                    for (JsonElement elem : stopsArray) {
                        BusStop s = gson.fromJson(elem, BusStop.class);
                        addStopRaw(s);
                        if (s.getStopId() > stopIdGen.get()) stopIdGen.set(s.getStopId());
                    }
                }

                if (seedObj.has("routes")) {
                    JsonArray routesArray = seedObj.getAsJsonArray("routes");
                    for (JsonElement elem : routesArray) {
                        BusRoute r = gson.fromJson(elem, BusRoute.class);
                        addRouteRaw(r);
                        if (r.getRouteId() > routeIdGen.get()) routeIdGen.set(r.getRouteId());
                    }
                }

                if (seedObj.has("segments")) {
                    JsonArray segmentsArray = seedObj.getAsJsonArray("segments");
                    for (JsonElement elem : segmentsArray) {
                        RouteSegment seg = gson.fromJson(elem, RouteSegment.class);
                        segments.add(seg);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load seed-data.json: " + e.getMessage());
            }
        }
    }

    private void addStopRaw(BusStop s) {
        stops.put(s.getStopId(), s);
    }

    private void addRouteRaw(BusRoute r) {
        routes.put(r.getRouteId(), r);
    }

    // Stops API
    public List<BusStop> getAllStops() {
        List<BusStop> list = new ArrayList<>(stops.values());
        list.sort(Comparator.comparing(BusStop::getName));
        return list;
    }

    public BusStop getStopById(int id) {
        return stops.get(id);
    }

    public synchronized BusStop createStop(String name, String area, double lat, double lon) {
        int id = stopIdGen.incrementAndGet();
        BusStop s = new BusStop(id, name, area, lat, lon);
        stops.put(id, s);
        return s;
    }

    public synchronized boolean deleteStop(int id) {
        if (stops.remove(id) != null) {
            segments.removeIf(seg -> seg.getFromStopId() == id || seg.getToStopId() == id);
            return true;
        }
        return false;
    }

    // Routes API
    public List<BusRoute> getAllRoutes() {
        List<BusRoute> list = new ArrayList<>(routes.values());
        list.sort(Comparator.comparing(BusRoute::getRouteName));
        return list;
    }

    public BusRoute getRouteById(int id) {
        return routes.get(id);
    }

    public synchronized BusRoute createRoute(String routeName, String operatorName, double farePerKm) {
        int id = routeIdGen.incrementAndGet();
        BusRoute r = new BusRoute(id, routeName, operatorName, farePerKm);
        routes.put(id, r);
        return r;
    }

    public synchronized boolean deleteRoute(int id) {
        if (routes.remove(id) != null) {
            segments.removeIf(seg -> seg.getRouteId() == id);
            return true;
        }
        return false;
    }

    // Segments API
    public List<RouteSegment> getAllSegments() {
        synchronized (segments) {
            return new ArrayList<>(segments);
        }
    }

    public synchronized void addSegment(RouteSegment seg) {
        segments.add(seg);
    }

    // Users & Auth
    public User authenticate(String email, String password) {
        User u = users.get(email);
        if (u != null && BCrypt.checkpw(password, u.getPassword())) {
            return u;
        }
        return null;
    }
}
