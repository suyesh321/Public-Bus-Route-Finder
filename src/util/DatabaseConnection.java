package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that opens a JDBC connection to the MySQL database used
 * by the Public Bus Route Finder - Kathmandu Valley application.
 *
 * Update URL / USER / PASSWORD below to match your local MySQL setup
 * before running the app.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/kathmandu_bus_db?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final String URL =
            System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DEFAULT_URL;
    private static final String USER =
            System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DEFAULT_USER;
    private static final String PASSWORD =
            System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASSWORD;

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to kathmandu_bus_db.", e);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
