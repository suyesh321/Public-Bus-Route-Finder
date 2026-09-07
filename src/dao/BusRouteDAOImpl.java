package dao;

import model.BusRoute;
import model.BusStop;
import model.RouteSegment;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusRouteDAOImpl implements BusRouteDAO {

    private final BusStopDAO stopDAO = new BusStopDAOImpl();

    @Override
    public List<BusRoute> getAllRoutes() {
        List<BusRoute> routes = new ArrayList<>();
        String sql = "SELECT route_id, route_name, operator_name, fare_per_km FROM bus_routes ORDER BY route_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BusRoute route = new BusRoute(
                        rs.getInt("route_id"),
                        rs.getString("route_name"),
                        rs.getString("operator_name"),
                        rs.getDouble("fare_per_km")
                );
                route.setStops(getStopsForRoute(route.getRouteId()));
                routes.add(route);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    public BusRoute getRouteById(int routeId) {
        String sql = "SELECT route_id, route_name, operator_name, fare_per_km FROM bus_routes WHERE route_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BusRoute route = new BusRoute(
                            rs.getInt("route_id"),
                            rs.getString("route_name"),
                            rs.getString("operator_name"),
                            rs.getDouble("fare_per_km")
                    );
                    route.setStops(getStopsForRoute(routeId));
                    return route;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<RouteSegment> getAllSegments() {
        List<RouteSegment> segments = new ArrayList<>();

        String sql = "SELECT rs.route_id, r.route_name, rs.from_stop_id, rs.to_stop_id, " +
                "rs.distance_km, rs.fare_npr, rs.sequence_order " +
                "FROM route_segments rs " +
                "JOIN bus_routes r ON rs.route_id = r.route_id " +
                "ORDER BY rs.route_id, rs.sequence_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                segments.add(new RouteSegment(
                        rs.getInt("route_id"),
                        rs.getString("route_name"),
                        rs.getInt("from_stop_id"),
                        rs.getInt("to_stop_id"),
                        rs.getDouble("distance_km"),
                        rs.getDouble("fare_npr"),
                        rs.getInt("sequence_order")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return segments;
    }

    @Override
    public boolean addRoute(BusRoute route) {
        String sql = "INSERT INTO bus_routes (route_name, operator_name, fare_per_km) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, route.getRouteName());
            ps.setString(2, route.getOperatorName());
            ps.setDouble(3, route.getFarePerKm());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        route.setRouteId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addSegment(RouteSegment segment) {
        String sql = "INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance_km, fare_npr, sequence_order) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, segment.getRouteId());
            ps.setInt(2, segment.getFromStopId());
            ps.setInt(3, segment.getToStopId());
            ps.setDouble(4, segment.getDistanceKm());
            ps.setDouble(5, segment.getFareNpr());
            ps.setInt(6, segment.getSequenceOrder());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteRoute(int routeId) {
        String deleteSegments = "DELETE FROM route_segments WHERE route_id = ?";
        String deleteRoute = "DELETE FROM bus_routes WHERE route_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteSegments);
                 PreparedStatement ps2 = conn.prepareStatement(deleteRoute)) {

                ps1.setInt(1, routeId);
                ps1.executeUpdate();

                ps2.setInt(1, routeId);
                int rows = ps2.executeUpdate();

                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<BusStop> getStopsForRoute(int routeId) {
        List<BusStop> stops = new ArrayList<>();
        String sql = "SELECT DISTINCT bs.stop_id, bs.name, bs.area, bs.latitude, bs.longitude, rs.sequence_order " +
                "FROM route_segments rs " +
                "JOIN bus_stops bs ON bs.stop_id = rs.from_stop_id " +
                "WHERE rs.route_id = ? " +
                "ORDER BY rs.sequence_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stops.add(new BusStop(
                            rs.getInt("stop_id"),
                            rs.getString("name"),
                            rs.getString("area"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stops;
    }
}
