package dao;

import model.BusStop;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusStopDAOImpl implements BusStopDAO {

    @Override
    public List<BusStop> getAllStops() {
        List<BusStop> stops = new ArrayList<>();
        String sql = "SELECT stop_id, name, area, latitude, longitude FROM bus_stops ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stops.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stops;
    }

    @Override
    public BusStop getStopById(int stopId) {
        String sql = "SELECT stop_id, name, area, latitude, longitude FROM bus_stops WHERE stop_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<BusStop> searchStopsByName(String keyword) {
        List<BusStop> stops = new ArrayList<>();
        String sql = "SELECT stop_id, name, area, latitude, longitude FROM bus_stops " +
                "WHERE name LIKE ? OR area LIKE ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stops.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stops;
    }

    @Override
    public boolean addStop(BusStop stop) {
        String sql = "INSERT INTO bus_stops (name, area, latitude, longitude) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stop.getName());
            ps.setString(2, stop.getArea());
            ps.setDouble(3, stop.getLatitude());
            ps.setDouble(4, stop.getLongitude());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStop(BusStop stop) {
        String sql = "UPDATE bus_stops SET name = ?, area = ?, latitude = ?, longitude = ? WHERE stop_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stop.getName());
            ps.setString(2, stop.getArea());
            ps.setDouble(3, stop.getLatitude());
            ps.setDouble(4, stop.getLongitude());
            ps.setInt(5, stop.getStopId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteStop(int stopId) {
        String sql = "DELETE FROM bus_stops WHERE stop_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stopId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private BusStop mapRow(ResultSet rs) throws SQLException {
        return new BusStop(
                rs.getInt("stop_id"),
                rs.getString("name"),
                rs.getString("area"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude")
        );
    }
}
