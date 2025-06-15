package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for Smart Parking System
 */
public class SmartParkingApp {
    private static final Logger LOGGER = Logger.getLogger(SmartParkingApp.class.getName());

    static {
        // Load the OpenCV native library at application startup
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            LOGGER.info("OpenCV loaded successfully: " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            LOGGER.log(Level.SEVERE, "Failed to load OpenCV native library. Camera functionality will not work.", e);
            JOptionPane.showMessageDialog(null,
                    "Failed to load OpenCV native library. Camera functionality will not work.\n" +
                            "Error: " + e.getMessage(),
                    "OpenCV Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Application entry point
     */
    public static void main(String[] args) {
        // Set up FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize FlatLaf", ex);
        }

        // Create necessary directories
        createRequiredDirectories();

        // Test database connection
        testDatabaseConnection();

        // Start the application
        SwingUtilities.invokeLater(() -> {
            // Show the login screen
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });
    }

    /**
     * Create necessary directories for the application
     */
    private static void createRequiredDirectories() {
        try {
            // Create directory for captured images
            java.io.File capturedImagesDir = new java.io.File("captured_images");
            if (!capturedImagesDir.exists()) {
                boolean created = capturedImagesDir.mkdirs();
                if (created) {
                    LOGGER.info("Created directory: captured_images");
                }
            }

            // Create tessdata directory for Tesseract
            java.io.File tessdataDir = new java.io.File("tessdata");
            if (!tessdataDir.exists()) {
                boolean created = tessdataDir.mkdirs();
                if (created) {
                    LOGGER.info("Created directory: tessdata");
                    LOGGER.warning("Please place Tesseract language files in the tessdata directory");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating application directories", e);
        }
    }

    /**
     * Test the database connection at startup
     */
    private static void testDatabaseConnection() {
        try {
            DatabaseConnection.getConnection();
            LOGGER.info("Database connection test successful");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);

            // Show error dialog
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Database connection failed. Please ensure MySQL is running and the database is set up.\n" +
                                "Error: " + e.getMessage() + "\n\n" +
                                "You can run the database/smart_parking_schema.sql script to set up the database.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}