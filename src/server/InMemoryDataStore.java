package server;

import model.BusRoute;
import model.BusStop;
import model.RouteSegment;
import model.User;
import model.enums.UserRole;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory repository initialized with default Kathmandu Valley
 * transit data from sql/schema.sql. This ensures the web application runs
 * immediately without requiring external MySQL setup, while also allowing
 * runtime additions and deletions from the Admin console.
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
        // Users
        users.put("admin@ktmbus.gov.np", new User(1, "Valley Transit Admin", "admin@ktmbus.gov.np", "admin123", UserRole.ADMIN));
        users.put("passenger@example.com", new User(2, "Sample Passenger", "passenger@example.com", "passenger123", UserRole.PASSENGER));

        // Bus stops
        addStopRaw(new BusStop(1, "Ratna Park", "Kathmandu", 27.7040, 85.3140));
        addStopRaw(new BusStop(2, "Sundhara", "Kathmandu", 27.6988, 85.3122));
        addStopRaw(new BusStop(3, "New Baneshwor", "Baneshwor", 27.6928, 85.3405));
        addStopRaw(new BusStop(4, "Koteshwor", "Koteshwor", 27.6776, 85.3486));
        addStopRaw(new BusStop(5, "Tribhuvan Airport", "Sinamangal", 27.6966, 85.3591));
        addStopRaw(new BusStop(6, "Kalanki", "Kalanki", 27.6939, 85.2809));
        addStopRaw(new BusStop(7, "Balkhu", "Kirtipur Road", 27.6858, 85.2966));
        addStopRaw(new BusStop(8, "Kalimati", "Kalimati", 27.6971, 85.3018));
        addStopRaw(new BusStop(9, "Balaju", "Balaju", 27.7280, 85.3050));
        addStopRaw(new BusStop(10, "Gongabu Bus Park", "Gongabu", 27.7326, 85.3193));
        addStopRaw(new BusStop(11, "Swayambhu", "Swayambhu", 27.7150, 85.2903));
        addStopRaw(new BusStop(12, "Lagankhel", "Lalitpur", 27.6667, 85.3235));
        addStopRaw(new BusStop(13, "Patan Dhoka", "Lalitpur", 27.6792, 85.3200));
        addStopRaw(new BusStop(14, "Jawalakhel", "Lalitpur", 27.6742, 85.3126));
        addStopRaw(new BusStop(15, "Bhaktapur Durbar Square", "Bhaktapur", 27.6710, 85.4285));
        addStopRaw(new BusStop(16, "Suryabinayak", "Bhaktapur", 27.6656, 85.4436));
        addStopRaw(new BusStop(17, "Chabahil", "Chabahil", 27.7175, 85.3459));
        addStopRaw(new BusStop(18, "Boudha", "Boudha", 27.7215, 85.3620));

        // Bus routes
        addRouteRaw(new BusRoute(1, "Route 1: Ratnapark - Airport - Koteshwor", "Sajha Yatayat", 4.5));
        addRouteRaw(new BusRoute(2, "Route 2: Ratnapark - Kalanki - Balkhu", "Nepal Yatayat", 4.0));
        addRouteRaw(new BusRoute(3, "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", "City Bus Service", 4.5));
        addRouteRaw(new BusRoute(4, "Route 4: Lagankhel - Patan Dhoka - Jawalakhel", "Sajha Yatayat", 4.0));
        addRouteRaw(new BusRoute(5, "Route 5: Koteshwor - Chabahil - Boudha", "Boudha Sewa Yatayat", 5.0));
        addRouteRaw(new BusRoute(6, "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", "Bhaktapur Yatayat", 5.5));

        // Route Segments
        // Route 1
        segments.add(new RouteSegment(1, "Route 1: Ratnapark - Airport - Koteshwor", 1, 2, 1.2, 15.0, 1));
        segments.add(new RouteSegment(1, "Route 1: Ratnapark - Airport - Koteshwor", 2, 3, 3.0, 15.0, 2));
        segments.add(new RouteSegment(1, "Route 1: Ratnapark - Airport - Koteshwor", 3, 5, 2.5, 15.0, 3));
        segments.add(new RouteSegment(1, "Route 1: Ratnapark - Airport - Koteshwor", 5, 4, 2.0, 15.0, 4));

        // Route 2
        segments.add(new RouteSegment(2, "Route 2: Ratnapark - Kalanki - Balkhu", 1, 8, 2.8, 15.0, 1));
        segments.add(new RouteSegment(2, "Route 2: Ratnapark - Kalanki - Balkhu", 8, 6, 2.6, 15.0, 2));
        segments.add(new RouteSegment(2, "Route 2: Ratnapark - Kalanki - Balkhu", 6, 7, 1.8, 15.0, 3));

        // Route 3
        segments.add(new RouteSegment(3, "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", 10, 9, 1.5, 15.0, 1));
        segments.add(new RouteSegment(3, "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", 9, 11, 2.2, 15.0, 2));
        segments.add(new RouteSegment(3, "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", 11, 8, 2.4, 15.0, 3));

        // Route 4
        segments.add(new RouteSegment(4, "Route 4: Lagankhel - Patan Dhoka - Jawalakhel", 12, 13, 1.6, 15.0, 1));
        segments.add(new RouteSegment(4, "Route 4: Lagankhel - Patan Dhoka - Jawalakhel", 13, 14, 1.4, 15.0, 2));

        // Route 5
        segments.add(new RouteSegment(5, "Route 5: Koteshwor - Chabahil - Boudha", 4, 17, 3.2, 20.0, 1));
        segments.add(new RouteSegment(5, "Route 5: Koteshwor - Chabahil - Boudha", 17, 18, 1.8, 15.0, 2));

        // Route 6
        segments.add(new RouteSegment(6, "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", 1, 3, 3.5, 20.0, 1));
        segments.add(new RouteSegment(6, "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", 3, 4, 1.6, 15.0, 2));
        segments.add(new RouteSegment(6, "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", 4, 16, 9.0, 35.0, 3));
        segments.add(new RouteSegment(6, "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", 16, 15, 2.0, 15.0, 4));
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
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        return null;
    }
}
