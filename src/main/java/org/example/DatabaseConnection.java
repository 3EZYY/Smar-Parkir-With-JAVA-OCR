
package org.example;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class to handle database connections and operations for the Smart Parking System
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    private static final String DB_USER = "parking_admin";
    private static final String DB_PASSWORD = "parkingadmin123";

    private static Connection connection = null;

    /**
     * Establishes a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish connection
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.info("Database connection established successfully");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage(), e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
            throw e;
        }
    }

    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }

    /**
     * Saves a vehicle entry to the database with logging
     * @param plateNumber The vehicle license plate number
     * @param vehicleType The type of vehicle (Motor, Mobil, Truk)
     * @param notes Optional notes about the vehicle
     * @return Generated vehicle ID if successful, -1 if failed
     */
    public static int saveVehicleEntry(String plateNumber, String vehicleType, String notes) {
        String insertQuery = "INSERT INTO parking_entries (plate_number, vehicle_type, entry_time, status, notes) " +
                "VALUES (?, ?, NOW(), 'ACTIVE', ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, plateNumber);
            stmt.setString(2, vehicleType);
            stmt.setString(3, notes != null && !notes.trim().isEmpty() ? notes : null);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        LOGGER.info("Vehicle entry saved successfully - ID: " + generatedId +
                                ", Plate: " + plateNumber + ", Type: " + vehicleType);
                        return generatedId;
                    }
                }
            }

            LOGGER.warning("Failed to save vehicle entry - no rows affected");
            return -1;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save vehicle entry for plate: " + plateNumber, e);
            return -1;
        }
    }

    /**
     * Updates vehicle exit information with logging
     * @param vehicleId The vehicle ID
     * @param durationMinutes Duration in minutes
     * @param fee Parking fee
     * @return true if successful, false otherwise
     */
    public static boolean updateVehicleExit(int vehicleId, long durationMinutes, double fee) {
        String updateQuery = "UPDATE parking_entries SET " +
                "exit_time = NOW(), " +
                "status = 'COMPLETED', " +
                "duration_minutes = ?, " +
                "fee = ? " +
                "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setLong(1, durationMinutes);
            stmt.setDouble(2, fee);
            stmt.setInt(3, vehicleId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info("Vehicle exit processed successfully - ID: " + vehicleId +
                        ", Duration: " + durationMinutes + " minutes, Fee: " + fee);
                return true;
            } else {
                LOGGER.warning("Failed to update vehicle exit - no rows affected for ID: " + vehicleId);
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update vehicle exit for ID: " + vehicleId, e);
            return false;
        }
    }

    /**
     * Updates vehicle data with logging
     * @param vehicleId The vehicle ID
     * @param plateNumber New plate number
     * @param vehicleType New vehicle type
     * @param notes New notes
     * @return true if successful, false otherwise
     */
    public static boolean updateVehicleData(int vehicleId, String plateNumber, String vehicleType, String notes) {
        String updateQuery = "UPDATE parking_entries SET " +
                "plate_number = ?, vehicle_type = ?, notes = ? " +
                "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, plateNumber);
            stmt.setString(2, vehicleType);
            stmt.setString(3, notes != null && !notes.trim().isEmpty() ? notes : null);
            stmt.setInt(4, vehicleId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info("Vehicle data updated successfully - ID: " + vehicleId +
                        ", Plate: " + plateNumber + ", Type: " + vehicleType);
                return true;
            } else {
                LOGGER.warning("Failed to update vehicle data - no rows affected for ID: " + vehicleId);
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update vehicle data for ID: " + vehicleId, e);
            return false;
        }
    }

    /**
     * Deletes vehicle record with logging
     * @param vehicleId The vehicle ID
     * @param plateNumber Plate number for logging purposes
     * @return true if successful, false otherwise
     */
    public static boolean deleteVehicleRecord(int vehicleId, String plateNumber) {
        String deleteQuery = "DELETE FROM parking_entries WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setInt(1, vehicleId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info("Vehicle record deleted successfully - ID: " + vehicleId + ", Plate: " + plateNumber);
                return true;
            } else {
                LOGGER.warning("Failed to delete vehicle record - no rows affected for ID: " + vehicleId);
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete vehicle record for ID: " + vehicleId, e);
            return false;
        }
    }

    /**
     * Saves a vehicle entry to the database with OCR scan data
     * @param plateNumber The vehicle license plate number
     * @param vehicleType The type of vehicle (Motor, Mobil, Truk)
     * @param notes Optional notes about the vehicle
     * @param imagePath Path to the captured image file
     * @param processedImagePath Path to the processed image file
     * @param ocrRawText Raw text from OCR before cleanup
     * @return Generated vehicle ID if successful, -1 if failed
     */
    public static int saveVehicleEntryWithOCR(String plateNumber, String vehicleType, String notes,
                                              String imagePath, String processedImagePath, String ocrRawText) {
        String insertQuery = "INSERT INTO parking_entries " +
                "(plate_number, vehicle_type, entry_time, status, notes, " +
                "original_image_path, processed_image_path, ocr_raw_text) " +
                "VALUES (?, ?, NOW(), 'ACTIVE', ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, plateNumber);
            stmt.setString(2, vehicleType);
            stmt.setString(3, notes != null && !notes.trim().isEmpty() ? notes : null);
            stmt.setString(4, imagePath);
            stmt.setString(5, processedImagePath);
            stmt.setString(6, ocrRawText);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        LOGGER.info("Vehicle entry with OCR data saved successfully - ID: " + generatedId +
                                ", Plate: " + plateNumber + ", Type: " + vehicleType +
                                ", Image: " + imagePath);
                        return generatedId;
                    }
                }
            }

            LOGGER.warning("Failed to save vehicle entry with OCR data - no rows affected");
            return -1;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save vehicle entry with OCR data for plate: " + plateNumber, e);
            return -1;
        }
    }

    /**
     * Saves scan session data
     * @param plateNumber Detected plate number
     * @param imagePath Original image path
     * @param processedImagePath Processed image path
     * @param ocrText OCR detected text
     * @param notes Session notes
     * @param isSuccessful Whether scan was successful
     * @return Generated session ID if successful, -1 if failed
     */
    public static int saveScanSession(String plateNumber, String imagePath, String processedImagePath,
                                      String ocrText, String notes, boolean isSuccessful) {
        String insertQuery = "INSERT INTO scan_sessions " +
                "(plate_number, original_image_path, processed_image_path, " +
                "ocr_text, notes, is_successful, scan_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, plateNumber);
            stmt.setString(2, imagePath);
            stmt.setString(3, processedImagePath);
            stmt.setString(4, ocrText);
            stmt.setString(5, notes);
            stmt.setBoolean(6, isSuccessful);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        LOGGER.info("Scan session saved successfully - ID: " + generatedId +
                                ", Plate: " + plateNumber + ", Success: " + isSuccessful);
                        return generatedId;
                    }
                }
            }

            LOGGER.warning("Failed to save scan session - no rows affected");
            return -1;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save scan session for plate: " + plateNumber, e);
            return -1;
        }
    }

    /**
     * Checks if a vehicle is currently parked
     * @param plateNumber The plate number to check
     * @return true if vehicle is parked (status ACTIVE), false otherwise
     */
    public static boolean isVehicleParked(String plateNumber) {
        String query = "SELECT COUNT(*) FROM parking_entries WHERE plate_number = ? AND status = 'ACTIVE'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, plateNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean isParked = rs.getInt(1) > 0;
                    LOGGER.info("Vehicle parking status checked - Plate: " + plateNumber + ", Parked: " + isParked);
                    return isParked;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check vehicle parking status for plate: " + plateNumber, e);
        }

        return false;
    }

    /**
     * Saves vehicle entry with scan data
     * @param plateNumber The vehicle license plate number
     * @param vehicleType The type of vehicle
     * @param notes Notes about the vehicle
     * @param imagePath Original image path
     * @param processedImagePath Processed image path
     * @param ocrText OCR detected text
     * @return Generated vehicle ID if successful, -1 if failed
     */
    public static int saveVehicleEntryWithScan(String plateNumber, String vehicleType, String notes,
                                               String imagePath, String processedImagePath, String ocrText) {
        String insertQuery = "INSERT INTO parking_entries " +
                "(plate_number, vehicle_type, entry_time, status, notes, " +
                "original_image_path, processed_image_path, ocr_raw_text) " +
                "VALUES (?, ?, NOW(), 'ACTIVE', ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, plateNumber);
            stmt.setString(2, vehicleType);
            stmt.setString(3, notes != null && !notes.trim().isEmpty() ? notes : null);
            stmt.setString(4, imagePath);
            stmt.setString(5, processedImagePath);
            stmt.setString(6, ocrText);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        LOGGER.info("Vehicle entry with scan data saved successfully - ID: " + generatedId +
                                ", Plate: " + plateNumber + ", Type: " + vehicleType);
                        return generatedId;
                    }
                }
            }

            LOGGER.warning("Failed to save vehicle entry with scan data - no rows affected");
            return -1;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save vehicle entry with scan data for plate: " + plateNumber, e);
            return -1;
        }
    }

    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn.isValid(5); // 5 second timeout
            if (isValid) {
                LOGGER.info("Database connection test successful");
            } else {
                LOGGER.warning("Database connection test failed - connection not valid");
            }
            return isValid;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }

    /**
     * Get database statistics with logging
     * @return DatabaseStats object containing statistics
     */
    public static DatabaseStats getDatabaseStats() {
        DatabaseStats stats = new DatabaseStats();

        try (Connection conn = getConnection()) {
            // Total vehicles today
            String todayQuery = "SELECT COUNT(*) FROM parking_entries WHERE DATE(entry_time) = CURDATE()";
            try (PreparedStatement stmt = conn.prepareStatement(todayQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.vehiclesToday = rs.getInt(1);
                }
            }

            // Active vehicles
            String activeQuery = "SELECT COUNT(*) FROM parking_entries WHERE status = 'ACTIVE'";
            try (PreparedStatement stmt = conn.prepareStatement(activeQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.activeVehicles = rs.getInt(1);
                }
            }

            // Completed vehicles today
            String completedQuery = "SELECT COUNT(*) FROM parking_entries WHERE status = 'COMPLETED' AND DATE(exit_time) = CURDATE()";
            try (PreparedStatement stmt = conn.prepareStatement(completedQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.completedToday = rs.getInt(1);
                }
            }

            // Revenue today
            String revenueQuery = "SELECT COALESCE(SUM(fee), 0) FROM parking_entries WHERE status = 'COMPLETED' AND DATE(exit_time) = CURDATE()";
            try (PreparedStatement stmt = conn.prepareStatement(revenueQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.revenueToday = rs.getDouble(1);
                }
            }

            LOGGER.info("Database statistics retrieved successfully");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve database statistics", e);
        }

        return stats;
    }

    /**
     * Inner class for database statistics
     */
    public static class DatabaseStats {
        public int vehiclesToday = 0;
        public int activeVehicles = 0;
        public int completedToday = 0;
        public double revenueToday = 0.0;
    }
}