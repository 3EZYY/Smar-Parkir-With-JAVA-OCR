package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPage() {
        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // Setting up the frame
        setTitle("Smart Parking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        // Create a header panel with logo and title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 122, 204));
        headerPanel.setPreferredSize(new Dimension(800, 100));

        JLabel titleLabel = new JLabel("SMART PARKING SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Login form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null); // Use absolute positioning
        formPanel.setBackground(new Color(240, 240, 240));

        // Create login container
        JPanel loginContainer = new JPanel();
        loginContainer.setLayout(null);
        loginContainer.setBounds(200, 30, 400, 300);
        loginContainer.setBackground(Color.WHITE);
        loginContainer.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // Login header
        JLabel loginHeader = new JLabel("Sign In");
        loginHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        loginHeader.setBounds(150, 20, 200, 30);

        // Username
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setBounds(50, 70, 300, 20);

        usernameField = new JTextField();
        usernameField.setBounds(50, 95, 300, 35);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setBounds(50, 140, 300, 20);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 165, 300, 35);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Login button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(50, 220, 300, 40);
        loginButton.setBackground(new Color(0, 122, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);

        // Add components to login container
        loginContainer.add(loginHeader);
        loginContainer.add(usernameLabel);
        loginContainer.add(usernameField);
        loginContainer.add(passwordLabel);
        loginContainer.add(passwordField);
        loginContainer.add(loginButton);

        // Add login container to form panel
        formPanel.add(loginContainer);

        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Login button action listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Cek database connection dulu
                try {
                    Connection conn = DatabaseConnection.getConnection();

                    // Query untuk cek user di database
                    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);

                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(LoginPage.this,
                                "Login Successful!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        new AdminDashboard().setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginPage.this,
                                "Invalid Username or Password", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(LoginPage.this,
                            "Database connection failed. Please start MySQL service.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginPage().setVisible(true);
            }
        });
    }
}