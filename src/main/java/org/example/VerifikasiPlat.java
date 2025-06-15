
package org.example;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class VerifikasiPlat extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(VerifikasiPlat.class.getName());

    private JTextField searchField;
    private JPanel resultPanel;
    private JPanel noResultPanel;
    private JLabel platValue;
    private JLabel waktuMasukValue;
    private JLabel tanggalMasukValue;
    private JLabel statusValue;
    private JLabel durasiValue;
    private JLabel biayaValue;
    private JLabel jenisKendaraanValue;
    private JLabel catatanValue;
    private JButton prosesKeluarButton;
    private JButton editDataButton;

    private Color accentColor = new Color(0, 122, 204);
    private boolean resultFound = false;

    // Data kendaraan yang ditemukan
    private int currentVehicleId = -1;
    private String currentPlateNumber = "";
    private String currentVehicleType = "";
    private Timestamp currentEntryTime = null;
    private long currentDurationMinutes = 0;
    private double currentFee = 0;

    public VerifikasiPlat() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        setTitle("Smart Parking - Verifikasi Plat Nomor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(800, 60));

        JLabel titleLabel = new JLabel("Verifikasi Plat Nomor Kendaraan");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Pencarian Kendaraan",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        JLabel searchLabel = new JLabel("Plat Nomor:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Add Enter key listener
        searchField.addActionListener(e -> verifyPlate());

        JButton searchButton = new JButton("Verifikasi");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(accentColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setPreferredSize(new Dimension(120, 35));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> verifyPlate());

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setBackground(new Color(95, 95, 95));
        resetButton.setForeground(Color.WHITE);
        resetButton.setPreferredSize(new Dimension(80, 35));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetForm());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        // Result panel (when vehicle found)
        resultPanel = new JPanel(new BorderLayout(0, 10));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(76, 175, 80)),
                "Data Kendaraan",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(76, 175, 80)
        ));
        resultPanel.setVisible(false);

        JPanel resultContent = new JPanel();
        resultContent.setLayout(new BoxLayout(resultContent, BoxLayout.Y_AXIS));
        resultContent.setBackground(Color.WHITE);
        resultContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Vehicle info panel
        JPanel vehicleInfoPanel = new JPanel(new GridLayout(8, 2, 10, 15));
        vehicleInfoPanel.setBackground(Color.WHITE);
        vehicleInfoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Informasi Kendaraan",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
        ));
        vehicleInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create labels and values
        addLabelValuePair(vehicleInfoPanel, "Plat Nomor:", platValue = new JLabel("-"));
        addLabelValuePair(vehicleInfoPanel, "Jenis Kendaraan:", jenisKendaraanValue = new JLabel("-"));
        addLabelValuePair(vehicleInfoPanel, "Waktu Masuk:", waktuMasukValue = new JLabel("-"));
        addLabelValuePair(vehicleInfoPanel, "Tanggal Masuk:", tanggalMasukValue = new JLabel("-"));
        addLabelValuePair(vehicleInfoPanel, "Status:", statusValue = new JLabel("-"));
        addLabelValuePair(vehicleInfoPanel, "Durasi Parkir:", durasiValue = new JLabel("-"));
        addLabelValuePair(vehicleInfoPanel, "Estimasi Biaya:", biayaValue = new JLabel("-"));
        addLabelValuePair(vehicleInfoPanel, "Catatan:", catatanValue = new JLabel("-"));

        platValue.setForeground(new Color(76, 175, 80));
        platValue.setFont(new Font("Segoe UI", Font.BOLD, 14));

        statusValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        biayaValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        biayaValue.setForeground(new Color(255, 87, 34));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        prosesKeluarButton = new JButton("Proses Keluar Kendaraan");
        prosesKeluarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        prosesKeluarButton.setBackground(new Color(76, 175, 80));
        prosesKeluarButton.setForeground(Color.WHITE);
        prosesKeluarButton.setPreferredSize(new Dimension(200, 40));
        prosesKeluarButton.setFocusPainted(false);
        prosesKeluarButton.addActionListener(e -> processExit());

        editDataButton = new JButton("Edit Data");
        editDataButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        editDataButton.setBackground(new Color(255, 193, 7));
        editDataButton.setForeground(Color.WHITE);
        editDataButton.setPreferredSize(new Dimension(120, 40));
        editDataButton.setFocusPainted(false);
        editDataButton.addActionListener(e -> editVehicleData());

        buttonPanel.add(prosesKeluarButton);
        buttonPanel.add(editDataButton);

        // Add panels to result content
        resultContent.add(vehicleInfoPanel);
        resultContent.add(Box.createVerticalStrut(15));
        resultContent.add(buttonPanel);

        resultPanel.add(resultContent, BorderLayout.CENTER);

        // No Result Panel (when vehicle not found)
        noResultPanel = new JPanel(new BorderLayout());
        noResultPanel.setBackground(Color.WHITE);
        noResultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(244, 67, 54)),
                "Hasil Pencarian",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(244, 67, 54)
        ));
        noResultPanel.setVisible(false);

        JPanel noResultContent = new JPanel();
        noResultContent.setLayout(new BoxLayout(noResultContent, BoxLayout.Y_AXIS));
        noResultContent.setBackground(Color.WHITE);
        noResultContent.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel noResultLabel = new JLabel("Kendaraan tidak ditemukan!");
        noResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        noResultLabel.setForeground(new Color(244, 67, 54));
        noResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel suggestionLabel = new JLabel("Kendaraan mungkin sudah keluar atau belum terdaftar.");
        suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        suggestionLabel.setForeground(Color.GRAY);
        suggestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = new JButton("Daftar Kendaraan Baru");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerButton.setBackground(accentColor);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> registerNewVehicle());

        noResultContent.add(noResultLabel);
        noResultContent.add(Box.createVerticalStrut(10));
        noResultContent.add(suggestionLabel);
        noResultContent.add(Box.createVerticalStrut(20));
        noResultContent.add(registerButton);

        noResultPanel.add(noResultContent, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Informasi Sistem",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        JTextArea infoTextArea = new JTextArea(
                "• Masukkan plat nomor kendaraan untuk memverifikasi status parkir\n" +
                        "• Sistem akan menampilkan informasi detail kendaraan jika ditemukan\n" +
                        "• Gunakan tombol 'Proses Keluar' untuk menyelesaikan pembayaran\n" +
                        "• Jika kendaraan tidak ditemukan, dapat mendaftarkan sebagai kendaraan baru"
        );
        infoTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoTextArea.setBackground(new Color(240, 248, 255));
        infoTextArea.setEditable(false);
        infoTextArea.setLineWrap(true);
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        infoPanel.add(infoTextArea, BorderLayout.CENTER);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);

        // Main content area
        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 10));
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.add(searchPanel, BorderLayout.NORTH);

        // Result container with CardLayout
        JPanel resultContainer = new JPanel(new CardLayout());
        resultContainer.add(new JPanel(), "empty");
        resultContainer.add(resultPanel, "result");
        resultContainer.add(noResultPanel, "noresult");
        mainContentPanel.add(resultContainer, BorderLayout.CENTER);

        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
        contentPanel.add(infoPanel, BorderLayout.SOUTH);

        // Add all components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Set initial focus
        searchField.requestFocus();
    }

    private void addLabelValuePair(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(label);
        panel.add(valueLabel);
    }

    private void verifyPlate() {
        String plateNumber = searchField.getText().trim().toUpperCase();

        if (plateNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Silakan masukkan plat nomor kendaraan!",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            searchField.requestFocus();
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Query untuk mencari kendaraan yang masih aktif
            String query = "SELECT id, plate_number, vehicle_type, entry_time, status, notes " +
                    "FROM parking_entries " +
                    "WHERE REPLACE(UPPER(plate_number), ' ', '') = REPLACE(?, ' ', '') " +
                    "AND status = 'ACTIVE' " +
                    "ORDER BY entry_time DESC " +
                    "LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, plateNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Kendaraan ditemukan
                        currentVehicleId = rs.getInt("id");
                        currentPlateNumber = rs.getString("plate_number");
                        currentVehicleType = rs.getString("vehicle_type");
                        currentEntryTime = rs.getTimestamp("entry_time");
                        String notes = rs.getString("notes");

                        // Update UI dengan data kendaraan
                        updateVehicleDisplay(notes);

                        // Show result panel
                        CardLayout cardLayout = (CardLayout) ((JPanel) resultPanel.getParent()).getLayout();
                        cardLayout.show(resultPanel.getParent(), "result");

                        resultFound = true;
                        LOGGER.info("Vehicle verified successfully: " + currentPlateNumber + " (ID: " + currentVehicleId + ")");

                    } else {
                        // Kendaraan tidak ditemukan
                        showNotFoundResult();
                        resultFound = false;
                        LOGGER.info("Vehicle not found: " + plateNumber);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Database error during vehicle verification: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error saat mencari data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateVehicleDisplay(String notes) {
        // Update detail kendaraan
        platValue.setText(currentPlateNumber);
        jenisKendaraanValue.setText(currentVehicleType);

        // Format waktu dan tanggal masuk
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        waktuMasukValue.setText(timeFormat.format(currentEntryTime));
        tanggalMasukValue.setText(dateFormat.format(currentEntryTime));

        statusValue.setText("AKTIF");
        statusValue.setForeground(new Color(76, 175, 80));

        // Hitung durasi parkir
        Date now = new Date();
        long durationMillis = now.getTime() - currentEntryTime.getTime();
        currentDurationMinutes = durationMillis / (1000 * 60);
        long hours = currentDurationMinutes / 60;
        long minutes = currentDurationMinutes % 60;

        String durationText = String.format("%d jam %d menit", hours, minutes);
        durasiValue.setText(durationText);

        // Hitung dan tampilkan biaya
        currentFee = calculateParkingFee(currentVehicleType, currentDurationMinutes);
        DecimalFormat df = new DecimalFormat("#,###");
        biayaValue.setText("Rp " + df.format(currentFee));

        // Catatan
        catatanValue.setText(notes != null && !notes.trim().isEmpty() ? notes : "-");

        // Show result panel
        resultPanel.setVisible(true);
        noResultPanel.setVisible(false);
    }

    private double calculateParkingFee(String vehicleType, long durationMinutes) {
        // Konversi menit ke jam (round up)
        int hours = (int) Math.ceil(durationMinutes / 60.0);
        if (hours < 1) hours = 1; // Minimal 1 jam

        double fee = 0;

        switch (vehicleType.toUpperCase()) {
            case "MOTOR":
                fee = 2000; // Jam pertama
                if (hours > 1) {
                    fee += (hours - 1) * 1000; // Jam berikutnya
                }
                break;
            case "MOBIL":
                fee = 5000; // Jam pertama
                if (hours > 1) {
                    fee += (hours - 1) * 2000; // Jam berikutnya
                }
                break;
            case "TRUK":
                fee = 10000; // Jam pertama
                if (hours > 1) {
                    fee += (hours - 1) * 5000; // Jam berikutnya
                }
                break;
            default:
                fee = 5000; // Default seperti mobil
                if (hours > 1) {
                    fee += (hours - 1) * 2000;
                }
                break;
        }

        return fee;
    }

    private void showNotFoundResult() {
        // Show no result panel
        resultPanel.setVisible(false);
        noResultPanel.setVisible(true);

        CardLayout cardLayout = (CardLayout) ((JPanel) noResultPanel.getParent()).getLayout();
        cardLayout.show(noResultPanel.getParent(), "noresult");
    }

    private void processExit() {
        if (!resultFound || currentVehicleId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Tidak ada data kendaraan yang valid untuk diproses!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        DecimalFormat df = new DecimalFormat("#,###");
        String confirmMessage = "Konfirmasi Keluar Kendaraan\n\n" +
                "Plat Nomor: " + currentPlateNumber + "\n" +
                "Jenis: " + currentVehicleType + "\n" +
                "Durasi: " + (currentDurationMinutes / 60) + " jam " + (currentDurationMinutes % 60) + " menit\n" +
                "Total Biaya: Rp " + df.format(currentFee) + "\n\n" +
                "Proses keluar kendaraan?";

        int confirm = JOptionPane.showConfirmDialog(this,
                confirmMessage,
                "Konfirmasi Keluar",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();

                // Update database
                String updateQuery = "UPDATE parking_entries SET " +
                        "exit_time = NOW(), " +
                        "status = 'COMPLETED', " +
                        "duration_minutes = ?, " +
                        "fee = ? " +
                        "WHERE id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setLong(1, currentDurationMinutes);
                    stmt.setDouble(2, currentFee);
                    stmt.setInt(3, currentVehicleId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Kendaraan berhasil diproses keluar!\n\n" +
                                        "Plat Nomor: " + currentPlateNumber + "\n" +
                                        "Total Biaya: Rp " + df.format(currentFee),
                                "Proses Berhasil",
                                JOptionPane.INFORMATION_MESSAGE);

                        LOGGER.info("Vehicle exit processed successfully: " + currentPlateNumber +
                                " (ID: " + currentVehicleId + "), Fee: " + currentFee);

                        // Reset form
                        resetForm();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Gagal memproses keluar kendaraan!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.severe("Failed to process vehicle exit: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Error database: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editVehicleData() {
        if (!resultFound || currentVehicleId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Tidak ada data kendaraan yang valid untuk diedit!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create edit dialog
        JDialog editDialog = new JDialog(this, "Edit Data Kendaraan", true);
        editDialog.setSize(400, 350);
        editDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Plat Nomor
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Plat Nomor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField plateField = new JTextField(currentPlateNumber);
        panel.add(plateField, gbc);

        // Jenis Kendaraan
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Jenis Kendaraan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] types = {"MOTOR", "MOBIL", "TRUK"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setSelectedItem(currentVehicleType);
        panel.add(typeCombo, gbc);

        // Catatan
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Catatan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        JTextArea notesArea = new JTextArea(catatanValue.getText().equals("-") ? "" : catatanValue.getText(), 3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        panel.add(notesScroll, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0; gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Simpan");
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> {
            updateVehicleData(plateField.getText().trim().toUpperCase(),
                    (String) typeCombo.getSelectedItem(),
                    notesArea.getText().trim());
            editDialog.dispose();
        });

        JButton cancelButton = new JButton("Batal");
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> editDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        editDialog.add(panel);
        editDialog.setVisible(true);
    }

    private void updateVehicleData(String plateNumber, String vehicleType, String notes) {
        if (plateNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Plat nomor tidak boleh kosong!",
                    "Validasi Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            String updateQuery = "UPDATE parking_entries SET " +
                    "plate_number = ?, vehicle_type = ?, notes = ? " +
                    "WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setString(1, plateNumber);
                stmt.setString(2, vehicleType);
                stmt.setString(3, notes.isEmpty() ? null : notes);
                stmt.setInt(4, currentVehicleId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Data kendaraan berhasil diupdate!",
                            "Update Berhasil",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Update current data
                    currentPlateNumber = plateNumber;
                    currentVehicleType = vehicleType;

                    // Refresh display
                    updateVehicleDisplay(notes.isEmpty() ? null : notes);

                    LOGGER.info("Vehicle data updated successfully for ID: " + currentVehicleId);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Gagal mengupdate data!",
                            "Update Gagal",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Failed to update vehicle data: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerNewVehicle() {
        String plateNumber = searchField.getText().trim().toUpperCase();

        // Buat form registrasi kendaraan baru yang sederhana
        ParkirManualNoHistory parkirManual = new ParkirManualNoHistory(plateNumber);
        parkirManual.setVisible(true);

        JOptionPane.showMessageDialog(this,
                "Form registrasi kendaraan baru telah dibuka.\n" +
                        "Data ini akan disimpan sebagai registrasi sementara.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);

        LOGGER.info("Manual parking form opened for new vehicle registration: " + plateNumber);
    }

    private void resetForm() {
        searchField.setText("");
        currentVehicleId = -1;
        currentPlateNumber = "";
        currentVehicleType = "";
        currentEntryTime = null;
        currentDurationMinutes = 0;
        currentFee = 0;
        resultFound = false;

        resultPanel.setVisible(false);
        noResultPanel.setVisible(false);

        CardLayout cardLayout = (CardLayout) ((JPanel) resultPanel.getParent()).getLayout();
        cardLayout.show(resultPanel.getParent(), "empty");

        searchField.requestFocus();
    }

    // Inner class yang independen untuk registrasi kendaraan baru
    private class ParkirManualNoHistory extends JDialog {

        private JTextField platNoField;
        private JComboBox<String> jenisKendaraanCombo;
        private JTextArea catatanArea;

        public ParkirManualNoHistory(String initialPlateNumber) {
            super(VerifikasiPlat.this, "Registrasi Kendaraan Baru", true);
            setSize(400, 350);
            setLocationRelativeTo(VerifikasiPlat.this);

            initComponents(initialPlateNumber);
        }

        private void initComponents(String initialPlateNumber) {
            setLayout(new BorderLayout());

            // Header panel
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(accentColor);
            headerPanel.setPreferredSize(new Dimension(400, 50));

            JLabel titleLabel = new JLabel("Registrasi Kendaraan Baru");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setBorder(new EmptyBorder(0, 15, 0, 0));
            headerPanel.add(titleLabel);

            // Form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            formPanel.setBackground(Color.WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;

            // Plat Nomor
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Plat Nomor:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            platNoField = new JTextField(initialPlateNumber);
            platNoField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            formPanel.add(platNoField, gbc);

            // Jenis Kendaraan
            gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Jenis Kendaraan:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            String[] types = {"MOTOR", "MOBIL", "TRUK"};
            jenisKendaraanCombo = new JComboBox<>(types);
            jenisKendaraanCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            formPanel.add(jenisKendaraanCombo, gbc);

            // Catatan
            gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Catatan:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
            catatanArea = new JTextArea("Registrasi melalui verifikasi plat", 3, 20);
            catatanArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            catatanArea.setLineWrap(true);
            catatanArea.setWrapStyleWord(true);
            JScrollPane catatanScroll = new JScrollPane(catatanArea);
            formPanel.add(catatanScroll, gbc);

            // Button panel
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0; gbc.weighty = 0; gbc.anchor = GridBagConstraints.CENTER;
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(Color.WHITE);

            JButton simpanButton = new JButton("Simpan");
            simpanButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            simpanButton.setBackground(new Color(76, 175, 80));
            simpanButton.setForeground(Color.WHITE);
            simpanButton.setFocusPainted(false);
            simpanButton.addActionListener(e -> simpanData());

            JButton batalButton = new JButton("Batal");
            batalButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            batalButton.setBackground(new Color(158, 158, 158));
            batalButton.setForeground(Color.WHITE);
            batalButton.setFocusPainted(false);
            batalButton.addActionListener(e -> dispose());

            buttonPanel.add(simpanButton);
            buttonPanel.add(batalButton);
            formPanel.add(buttonPanel, gbc);

            add(headerPanel, BorderLayout.NORTH);
            add(formPanel, BorderLayout.CENTER);
        }

        private void simpanData() {
            if (!validateInput()) {
                return;
            }

            String plateNumber = platNoField.getText().trim().toUpperCase();
            String vehicleType = (String) jenisKendaraanCombo.getSelectedItem();
            String notes = catatanArea.getText().trim();

            try {
                // Simpan ke database sebagai kendaraan aktif
                int vehicleId = DatabaseConnection.saveVehicleEntryWithScan(
                        plateNumber,
                        vehicleType,
                        notes,
                        "", // No image path
                        "", // No processed image path
                        "Manual registration via verification"
                );

                if (vehicleId > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Kendaraan berhasil didaftarkan!\n\n" +
                                    "Plat Nomor: " + plateNumber + "\n" +
                                    "Jenis: " + vehicleType + "\n" +
                                    "ID: " + vehicleId + "\n" +
                                    "Status: ACTIVE",
                            "Registrasi Berhasil",
                            JOptionPane.INFORMATION_MESSAGE);

                    LOGGER.info("New vehicle registered: " + plateNumber + " (" + vehicleType + "), ID: " + vehicleId);
                    dispose();

                    // Refresh parent form untuk menampilkan kendaraan yang baru didaftarkan
                    searchField.setText(plateNumber);
                    verifyPlate();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Gagal mendaftarkan kendaraan. Silakan coba lagi.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                LOGGER.severe("Failed to register new vehicle: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Error saat mendaftarkan kendaraan: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private boolean validateInput() {
            String plateNumber = platNoField.getText().trim();

            if (plateNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Plat nomor tidak boleh kosong!",
                        "Validasi Error",
                        JOptionPane.WARNING_MESSAGE);
                platNoField.requestFocus();
                return false;
            }

            if (plateNumber.length() < 3) {
                JOptionPane.showMessageDialog(this,
                        "Plat nomor terlalu pendek! Minimal 3 karakter.",
                        "Validasi Error",
                        JOptionPane.WARNING_MESSAGE);
                platNoField.requestFocus();
                return false;
            }

            // Check if vehicle already exists
            try {
                if (DatabaseConnection.isVehicleParked(plateNumber)) {
                    JOptionPane.showMessageDialog(this,
                            "Kendaraan dengan plat nomor " + plateNumber + " sudah terdaftar!",
                            "Validasi Error",
                            JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to check existing vehicle: " + e.getMessage());
                // Continue with registration if check fails
            }

            return true;
        }
    }
}