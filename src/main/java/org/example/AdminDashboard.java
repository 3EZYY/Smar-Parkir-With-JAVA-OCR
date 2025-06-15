package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AdminDashboard extends JFrame {

    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private Map<String, JPanel> panels;
    private Color accentColor = new Color(0, 122, 204);
    private Color hoverColor = new Color(230, 240, 250);

    // Dashboard stat labels - untuk update data real-time
    private JLabel kendaraanMasukHariIniLabel;
    private JLabel kendaraanKeluarHariIniLabel;
    private JLabel totalKendaraanTerparkirLabel;
    private JLabel pendapatanHariIniLabel;

    // Timer untuk auto refresh
    private Timer dashboardTimer;

    public AdminDashboard() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        setTitle("Smart Parking System - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        initComponents();
        startDashboardTimer();
    }

    private void initComponents() {
        // Set BorderLayout for the frame
        setLayout(new BorderLayout());

        // Create header
        add(createHeader(), BorderLayout.NORTH);

        // Create sidebar
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        // Initialize content panel
        contentPanel = new JPanel(new CardLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Initialize panels map
        panels = new HashMap<>();

        // Create panels for different sections
        initPanels();

        // Show dashboard panel by default
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "Dashboard");
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(1200, 60));

        JLabel titleLabel = new JLabel("Smart Parking System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        JLabel userLabel = new JLabel("Administrator | ");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        AdminDashboard.this,
                        "Apakah Anda yakin ingin logout?",
                        "Konfirmasi Logout",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    stopDashboardTimer();
                    dispose();
                    new LoginPage().setVisible(true);
                }
            }
        });

        rightPanel.add(userLabel);
        rightPanel.add(logoutButton);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        // Add logo or icon at the top
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setMaximumSize(new Dimension(220, 100));
        logoPanel.setBackground(Color.WHITE);

        JLabel logoLabel = new JLabel("ADMIN PANEL");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoLabel.setForeground(accentColor);
        logoPanel.add(logoLabel);

        sidebar.add(logoPanel);

        // Add separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(220, 1));
        sidebar.add(separator);

        // Create menu items
        String[] menuItems = {"Dashboard", "Scan Plat Nomor", "Verifikasi Plat", "Parkir Manual", "Riwayat Parkir", "Cek Tarif"};

        for (String item : menuItems) {
            JPanel menuItem = createMenuItem(item);
            sidebar.add(menuItem);

            // Add click listener
            menuItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Show the corresponding panel
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, item);

                    // Refresh dashboard data when dashboard is selected
                    if (item.equals("Dashboard")) {
                        updateDashboardData();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!menuItem.getBackground().equals(accentColor)) {
                        menuItem.setBackground(hoverColor);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!menuItem.getBackground().equals(accentColor)) {
                        menuItem.setBackground(Color.WHITE);
                    }
                }
            });
        }

        // Add empty filler at the bottom to push items to the top
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JPanel createMenuItem(String title) {
        JPanel menuItem = new JPanel(new BorderLayout());
        menuItem.setMaximumSize(new Dimension(220, 40));
        menuItem.setBackground(Color.WHITE);

        // Set background color for the Dashboard menu item by default
        if (title.equals("Dashboard")) {
            menuItem.setBackground(accentColor);
        }

        // Create icon placeholder (you can replace with actual icons)
        JLabel iconLabel = new JLabel("  •  ");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        iconLabel.setForeground(title.equals("Dashboard") ? Color.WHITE : accentColor);

        // Create menu text
        JLabel textLabel = new JLabel(title);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(title.equals("Dashboard") ? Color.WHITE : Color.DARK_GRAY);

        menuItem.add(iconLabel, BorderLayout.WEST);
        menuItem.add(textLabel, BorderLayout.CENTER);

        // Add some padding
        menuItem.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        return menuItem;
    }

    private void initPanels() {
        // Create dashboard panel
        JPanel dashboardPanel = createDashboardPanel();
        contentPanel.add(dashboardPanel, "Dashboard");
        panels.put("Dashboard", dashboardPanel);

        // Create other panels
        JPanel scanPlatPanel = createScanPlatPanel();
        contentPanel.add(scanPlatPanel, "Scan Plat Nomor");
        panels.put("Scan Plat Nomor", scanPlatPanel);

        JPanel verifikasiPlatPanel = createVerifikasiPlatPanel();
        contentPanel.add(verifikasiPlatPanel, "Verifikasi Plat");
        panels.put("Verifikasi Plat", verifikasiPlatPanel);

        JPanel parkirManualPanel = createParkirManualPanel();
        contentPanel.add(parkirManualPanel, "Parkir Manual");
        panels.put("Parkir Manual", parkirManualPanel);

        JPanel riwayatParkirPanel = createRiwayatParkirPanel();
        contentPanel.add(riwayatParkirPanel, "Riwayat Parkir");
        panels.put("Riwayat Parkir", riwayatParkirPanel);

        JPanel cekTarifPanel = createCekTarifPanel();
        contentPanel.add(cekTarifPanel, "Cek Tarif");
        panels.put("Cek Tarif", cekTarifPanel);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.DARK_GRAY);

        JLabel subtitleLabel = new JLabel("Overview sistem parkir");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setOpaque(false);
        labelPanel.add(titleLabel);
        labelPanel.add(subtitleLabel);

        headerPanel.add(labelPanel, BorderLayout.WEST);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(new Color(245, 245, 245));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add stat cards dengan reference ke label untuk update data
        JPanel kendaraanMasukCard = createStatCard("Kendaraan Masuk Hari Ini", "0", new Color(0, 153, 255));
        kendaraanMasukHariIniLabel = getValueLabelFromCard(kendaraanMasukCard);
        statsPanel.add(kendaraanMasukCard);

        JPanel kendaraanKeluarCard = createStatCard("Kendaraan Keluar Hari Ini", "0", new Color(0, 204, 102));
        kendaraanKeluarHariIniLabel = getValueLabelFromCard(kendaraanKeluarCard);
        statsPanel.add(kendaraanKeluarCard);

        JPanel totalTerparkirCard = createStatCard("Total Kendaraan Terparkir", "0", new Color(255, 153, 0));
        totalKendaraanTerparkirLabel = getValueLabelFromCard(totalTerparkirCard);
        statsPanel.add(totalTerparkirCard);

        JPanel pendapatanCard = createStatCard("Pendapatan Hari Ini", "Rp 0", new Color(153, 51, 255));
        pendapatanHariIniLabel = getValueLabelFromCard(pendapatanCard);
        statsPanel.add(pendapatanCard);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Selamat datang di Sistem Smart Parking!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea infoArea = new JTextArea(
                "Sistem ini digunakan untuk mengelola parkir secara otomatis dengan fitur pengenalan " +
                        "plat nomor kendaraan. Silakan gunakan menu di sebelah kiri untuk mengakses " +
                        "fitur-fitur yang tersedia.\n\n" +
                        "• Scan Plat Nomor: Untuk mendeteksi plat nomor kendaraan masuk secara otomatis\n" +
                        "• Verifikasi Plat: Untuk memeriksa dan memproses kendaraan yang akan keluar\n" +
                        "• Parkir Manual: Untuk memasukkan data kendaraan secara manual\n" +
                        "• Riwayat Parkir: Untuk melihat riwayat kendaraan yang masuk dan keluar\n" +
                        "• Cek Tarif: Untuk memeriksa tarif parkir kendaraan"
        );
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBackground(Color.WHITE);

        contentPanel.add(welcomeLabel, BorderLayout.NORTH);
        contentPanel.add(infoArea, BorderLayout.CENTER);

        // Add all panels to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.SOUTH);

        // Load initial data
        updateDashboardData();

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        // Create colored top bar
        JPanel colorBar = new JPanel();
        colorBar.setBackground(color);
        colorBar.setPreferredSize(new Dimension(100, 5));
        card.add(colorBar, BorderLayout.NORTH);

        // Create card content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(valueLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(titleLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    // Helper method untuk mendapatkan value label dari stat card
    private JLabel getValueLabelFromCard(JPanel card) {
        JPanel contentPanel = (JPanel) card.getComponent(1); // Get content panel
        return (JLabel) contentPanel.getComponent(0); // Get value label
    }

    // Method untuk update data dashboard dari database
    private void updateDashboardData() {
        SwingUtilities.invokeLater(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection();

                // 1. Kendaraan masuk hari ini
                String queryMasukHariIni = "SELECT COUNT(*) FROM parking_entries WHERE DATE(entry_time) = CURDATE()";
                try (PreparedStatement stmt = conn.prepareStatement(queryMasukHariIni);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        kendaraanMasukHariIniLabel.setText(String.valueOf(count));
                    }
                }

                // 2. Kendaraan keluar hari ini
                String queryKeluarHariIni = "SELECT COUNT(*) FROM parking_entries WHERE DATE(exit_time) = CURDATE() AND status = 'COMPLETED'";
                try (PreparedStatement stmt = conn.prepareStatement(queryKeluarHariIni);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        kendaraanKeluarHariIniLabel.setText(String.valueOf(count));
                    }
                }

                // 3. Total kendaraan terparkir (status ACTIVE)
                String queryTerparkir = "SELECT COUNT(*) FROM parking_entries WHERE status = 'ACTIVE'";
                try (PreparedStatement stmt = conn.prepareStatement(queryTerparkir);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        totalKendaraanTerparkirLabel.setText(String.valueOf(count));
                    }
                }

                // 4. Pendapatan hari ini
                String queryPendapatan = "SELECT COALESCE(SUM(fee), 0) FROM parking_entries WHERE DATE(exit_time) = CURDATE() AND status = 'COMPLETED' AND fee IS NOT NULL";
                try (PreparedStatement stmt = conn.prepareStatement(queryPendapatan);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double pendapatan = rs.getDouble(1);
                        DecimalFormat df = new DecimalFormat("#,###");
                        pendapatanHariIniLabel.setText("Rp " + df.format(pendapatan));
                    }
                }

            } catch (SQLException e) {
                System.err.println("Error updating dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Method untuk start timer auto refresh
    private void startDashboardTimer() {
        dashboardTimer = new Timer();
        dashboardTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDashboardData();
            }
        }, 0, 30000); // Update setiap 30 detik
    }

    // Method untuk stop timer
    private void stopDashboardTimer() {
        if (dashboardTimer != null) {
            dashboardTimer.cancel();
            dashboardTimer = null;
        }
    }

    @Override
    public void dispose() {
        stopDashboardTimer();
        super.dispose();
    }

    private JPanel createScanPlatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Scan Plat Nomor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton openScanWindowButton = new JButton("Buka Aplikasi Scan Plat");
        openScanWindowButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openScanWindowButton.setBackground(accentColor);
        openScanWindowButton.setForeground(Color.WHITE);
        openScanWindowButton.setFocusPainted(false);
        openScanWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ScanPlatNomor().setVisible(true);
            }
        });
        headerPanel.add(openScanWindowButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("Tentang Fitur Scan Plat Nomor");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea infoArea = new JTextArea(
                "Fitur Scan Plat Nomor memungkinkan Anda untuk mendeteksi plat nomor kendaraan secara otomatis " +
                        "menggunakan kamera. Sistem akan menggunakan teknologi OCR (Optical Character Recognition) " +
                        "untuk mengenali karakter pada plat nomor.\n\n" +
                        "Cara menggunakan:\n" +
                        "1. Klik tombol 'Buka Aplikasi Scan Plat' di atas\n" +
                        "2. Aktifkan kamera dengan klik tombol 'Start Camera'\n" +
                        "3. Arahkan kamera ke plat nomor kendaraan\n" +
                        "4. Klik tombol 'Capture' untuk mengambil gambar\n" +
                        "5. Klik tombol 'Process OCR' untuk mendeteksi teks\n" +
                        "6. Periksa hasil dan simpan data kendaraan\n\n" +
                        "Tips: Pastikan pencahayaan cukup dan posisi kamera tepat untuk hasil yang optimal."
        );
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBackground(Color.WHITE);
        infoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(infoArea);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createVerifikasiPlatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Verifikasi Plat");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton openVerifikasiWindowButton = new JButton("Buka Aplikasi Verifikasi");
        openVerifikasiWindowButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openVerifikasiWindowButton.setBackground(accentColor);
        openVerifikasiWindowButton.setForeground(Color.WHITE);
        openVerifikasiWindowButton.setFocusPainted(false);
        openVerifikasiWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new VerifikasiPlat().setVisible(true);
            }
        });
        headerPanel.add(openVerifikasiWindowButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("Tentang Fitur Verifikasi Plat");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea infoArea = new JTextArea(
                "Fitur Verifikasi Plat digunakan untuk memeriksa dan memproses kendaraan yang akan keluar dari area parkir. " +
                        "Anda dapat mencari data kendaraan berdasarkan plat nomor dan melihat informasi lengkap seperti " +
                        "waktu masuk, durasi parkir, dan status pembayaran.\n\n" +
                        "Cara menggunakan:\n" +
                        "1. Klik tombol 'Buka Aplikasi Verifikasi' di atas\n" +
                        "2. Masukkan plat nomor kendaraan yang akan dicari\n" +
                        "3. Klik tombol 'Cari' untuk melihat informasi kendaraan\n" +
                        "4. Jika kendaraan ditemukan, Anda dapat memproses keluarnya kendaraan\n" +
                        "5. Sistem akan menghitung durasi parkir dan biaya yang harus dibayar\n\n" +
                        "Catatan: Pastikan pembayaran telah dilakukan sebelum memproses keluarnya kendaraan."
        );
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBackground(Color.WHITE);
        infoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(infoArea);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createParkirManualPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Parkir Manual");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton openManualWindowButton = new JButton("Buka Form Parkir Manual");
        openManualWindowButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openManualWindowButton.setBackground(accentColor);
        openManualWindowButton.setForeground(Color.WHITE);
        openManualWindowButton.setFocusPainted(false);
        openManualWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ParkirManual().setVisible(true);
            }
        });
        headerPanel.add(openManualWindowButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("Tentang Fitur Parkir Manual");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea infoArea = new JTextArea(
                "Fitur Parkir Manual memungkinkan Anda untuk memasukkan data kendaraan secara manual tanpa " +
                        "menggunakan kamera. Fitur ini berguna sebagai alternatif atau backup jika fitur Scan Plat Nomor " +
                        "tidak dapat digunakan karena masalah teknis.\n\n" +
                        "Cara menggunakan:\n" +
                        "1. Klik tombol 'Buka Form Parkir Manual' di atas\n" +
                        "2. Isi data kendaraan seperti plat nomor dan jenis kendaraan\n" +
                        "3. Tambahkan catatan jika diperlukan\n" +
                        "4. Klik tombol 'Simpan' untuk menyimpan data kendaraan\n\n" +
                        "Catatan: Pastikan data yang dimasukkan sudah benar untuk menghindari kesalahan dalam pengelolaan parkir."
        );
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBackground(Color.WHITE);
        infoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(infoArea);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRiwayatParkirPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Riwayat Parkir");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton openRiwayatWindowButton = new JButton("Lihat Riwayat Parkir");
        openRiwayatWindowButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openRiwayatWindowButton.setBackground(accentColor);
        openRiwayatWindowButton.setForeground(Color.WHITE);
        openRiwayatWindowButton.setFocusPainted(false);
        openRiwayatWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RiwayatParkir().setVisible(true);
            }
        });
        headerPanel.add(openRiwayatWindowButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("Tentang Fitur Riwayat Parkir");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea infoArea = new JTextArea(
                "Fitur Riwayat Parkir memungkinkan Anda untuk melihat catatan lengkap semua kendaraan yang pernah " +
                        "masuk dan keluar dari area parkir. Anda dapat melihat informasi seperti plat nomor, jenis kendaraan, " +
                        "waktu masuk, waktu keluar, durasi parkir, dan biaya yang dibayarkan.\n\n" +
                        "Cara menggunakan:\n" +
                        "1. Klik tombol 'Lihat Riwayat Parkir' di atas\n" +
                        "2. Gunakan filter untuk mencari data berdasarkan tanggal, status, atau plat nomor\n" +
                        "3. Data dapat diurutkan berdasarkan kolom yang diinginkan\n" +
                        "4. Anda juga dapat mengekspor data ke format Excel atau mencetak laporan\n\n" +
                        "Fitur ini sangat berguna untuk pelaporan dan analisis penggunaan area parkir."
        );
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBackground(Color.WHITE);
        infoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(infoArea);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCekTarifPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Cek Tarif");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton openTarifWindowButton = new JButton("Buka Aplikasi Cek Tarif");
        openTarifWindowButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openTarifWindowButton.setBackground(accentColor);
        openTarifWindowButton.setForeground(Color.WHITE);
        openTarifWindowButton.setFocusPainted(false);
        openTarifWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CekTarif().setVisible(true);
            }
        });
        headerPanel.add(openTarifWindowButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("Tentang Fitur Cek Tarif");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea infoArea = new JTextArea(
                "Fitur Cek Tarif digunakan untuk memeriksa dan menghitung biaya parkir yang harus dibayarkan oleh " +
                        "pengguna. Sistem akan menghitung biaya berdasarkan durasi parkir dan jenis kendaraan sesuai " +
                        "dengan tarif yang telah ditetapkan.\n\n" +
                        "Cara menggunakan:\n" +
                        "1. Klik tombol 'Buka Aplikasi Cek Tarif' di atas\n" +
                        "2. Masukkan plat nomor kendaraan yang akan dicek tarifnya\n" +
                        "3. Klik tombol 'Cek' untuk melihat informasi tarif\n" +
                        "4. Sistem akan menampilkan waktu masuk, durasi parkir, dan total biaya\n" +
                        "5. Setelah pembayaran, Anda dapat memproses transaksi\n\n" +
                        "Tarif parkir saat ini:\n" +
                        "• Motor: Rp. 2.000 (jam pertama) + Rp. 1.000 (per jam berikutnya)\n" +
                        "• Mobil: Rp. 5.000 (jam pertama) + Rp. 2.000 (per jam berikutnya)\n" +
                        "• Truk: Rp. 10.000 (jam pertama) + Rp. 5.000 (per jam berikutnya)"
        );
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBackground(Color.WHITE);
        infoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(infoArea);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminDashboard().setVisible(true);
        });
    }
}