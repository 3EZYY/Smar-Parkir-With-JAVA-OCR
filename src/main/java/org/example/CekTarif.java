package org.example;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.logging.Logger;

public class CekTarif extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(CekTarif.class.getName());

    private JComboBox<String> jenisKendaraanCombo;
    private JSpinner jamSpinner;
    private JSpinner menitSpinner;
    private JLabel totalBiayaLabel;
    private JTextArea detailBiayaArea;
    private JButton hitungButton;
    private JButton resetButton;
    private JButton verifikasiPlatButton;

    private Color accentColor = new Color(0, 122, 204);

    public CekTarif() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        setTitle("Smart Parking - Cek Tarif Parkir");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        initComponents();
        LOGGER.info("Tarif checking window opened");
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(700, 60));

        JLabel titleLabel = new JLabel("Kalkulator Tarif Parkir");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Input Data Parkir",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Jenis Kendaraan
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel jenisLabel = new JLabel("Jenis Kendaraan:");
        jenisLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(jenisLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] jenisKendaraan = {"MOTOR", "MOBIL", "TRUK"};
        jenisKendaraanCombo = new JComboBox<>(jenisKendaraan);
        jenisKendaraanCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jenisKendaraanCombo.setPreferredSize(new Dimension(200, 35));
        jenisKendaraanCombo.setBackground(Color.WHITE);
        jenisKendaraanCombo.addActionListener(e -> updateTarifInfo());
        inputPanel.add(jenisKendaraanCombo, gbc);

        // Durasi Parkir - Jam
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel jamLabel = new JLabel("Jam Parkir:");
        jamLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(jamLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        jamSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 24, 1));
        jamSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jamSpinner.setPreferredSize(new Dimension(200, 35));
        jamSpinner.addChangeListener(e -> calculateFee());
        inputPanel.add(jamSpinner, gbc);

        // Durasi Parkir - Menit
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel menitLabel = new JLabel("Menit Tambahan:");
        menitLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(menitLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        menitSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        menitSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        menitSpinner.setPreferredSize(new Dimension(200, 35));
        menitSpinner.addChangeListener(e -> calculateFee());
        inputPanel.add(menitSpinner, gbc);

        // Result panel
        JPanel resultPanel = new JPanel(new BorderLayout(0, 10));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(76, 175, 80)),
                "Hasil Perhitungan",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(76, 175, 80)
        ));

        // Total biaya
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        totalPanel.setBackground(Color.WHITE);

        JLabel totalLabel = new JLabel("Total Biaya: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        totalBiayaLabel = new JLabel("Rp 0");
        totalBiayaLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalBiayaLabel.setForeground(new Color(76, 175, 80));

        totalPanel.add(totalLabel);
        totalPanel.add(totalBiayaLabel);

        // Detail biaya
        detailBiayaArea = new JTextArea(8, 30);
        detailBiayaArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailBiayaArea.setEditable(false);
        detailBiayaArea.setBackground(new Color(248, 249, 250));
        detailBiayaArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        detailBiayaArea.setLineWrap(true);
        detailBiayaArea.setWrapStyleWord(true);

        JScrollPane detailScrollPane = new JScrollPane(detailBiayaArea);
        detailScrollPane.setBorder(BorderFactory.createTitledBorder("Detail Perhitungan"));

        resultPanel.add(totalPanel, BorderLayout.NORTH);
        resultPanel.add(detailScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        hitungButton = new JButton("Hitung Tarif");
        hitungButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        hitungButton.setBackground(accentColor);
        hitungButton.setForeground(Color.WHITE);
        hitungButton.setPreferredSize(new Dimension(130, 40));
        hitungButton.setFocusPainted(false);
        hitungButton.addActionListener(e -> calculateFee());

        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setBackground(new Color(158, 158, 158));
        resetButton.setForeground(Color.WHITE);
        resetButton.setPreferredSize(new Dimension(100, 40));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetForm());

        verifikasiPlatButton = new JButton("Verifikasi Plat");
        verifikasiPlatButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        verifikasiPlatButton.setBackground(new Color(255, 152, 0));
        verifikasiPlatButton.setForeground(Color.WHITE);
        verifikasiPlatButton.setPreferredSize(new Dimension(140, 40));
        verifikasiPlatButton.setFocusPainted(false);
        verifikasiPlatButton.addActionListener(e -> openVerifikasiPlat());

        JButton tambahPlatButton = new JButton("Tambah Plat");
        tambahPlatButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tambahPlatButton.setBackground(new Color(76, 175, 80));
        tambahPlatButton.setForeground(Color.WHITE);
        tambahPlatButton.setPreferredSize(new Dimension(130, 40));
        tambahPlatButton.setFocusPainted(false);
        tambahPlatButton.addActionListener(e -> openParkirManual());

        JButton tutupButton = new JButton("Tutup");
        tutupButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tutupButton.setBackground(new Color(244, 67, 54));
        tutupButton.setForeground(Color.WHITE);
        tutupButton.setPreferredSize(new Dimension(100, 40));
        tutupButton.setFocusPainted(false);
        tutupButton.addActionListener(e -> dispose());

        buttonPanel.add(hitungButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(verifikasiPlatButton);
        buttonPanel.add(tambahPlatButton);
        buttonPanel.add(tutupButton);

        // Tarif info panel
        JPanel tarifInfoPanel = createTarifInfoPanel();

        // Add all panels to main panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(resultPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Create split pane for tarif info
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPanel, tarifInfoPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.7);

        add(splitPane);

        // Initial calculation
        calculateFee();
        updateTarifInfo();
    }

    private JPanel createTarifInfoPanel() {
        JPanel tarifInfoPanel = new JPanel(new BorderLayout());
        tarifInfoPanel.setBackground(new Color(240, 248, 255));
        tarifInfoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(accentColor),
                "Daftar Tarif Parkir",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14),
                accentColor
        ));
        tarifInfoPanel.setPreferredSize(new Dimension(200, 600));

        JTextArea tarifTextArea = new JTextArea();
        tarifTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tarifTextArea.setBackground(new Color(240, 248, 255));
        tarifTextArea.setEditable(false);
        tarifTextArea.setLineWrap(true);
        tarifTextArea.setWrapStyleWord(true);
        tarifTextArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        String tarifInfo = " DAFTAR TARIF PARKIR\n\n" +
                "️ MOTOR:\n" +
                "• Jam pertama: Rp 2.000\n" +
                "• Jam berikutnya: Rp 1.000/jam\n\n" +
                " MOBIL:\n" +
                "• Jam pertama: Rp 5.000\n" +
                "• Jam berikutnya: Rp 2.000/jam\n\n" +
                " TRUK:\n" +
                "• Jam pertama: Rp 10.000\n" +
                "• Jam berikutnya: Rp 5.000/jam\n\n" +
                "⏰ KETENTUAN:\n" +
                "• Minimal parkir 1 jam\n" +
                "• Lebih dari 30 menit = 1 jam\n" +
                "• Kurang dari 30 menit = 0 jam\n\n" +
                " TIPS:\n" +
                "• Gunakan kalkulator ini untuk\n" +
                "  estimasi biaya parkir\n" +
                "• Klik 'Verifikasi Plat' untuk\n" +
                "  cek kendaraan yang parkir";

        tarifTextArea.setText(tarifInfo);

        JScrollPane scrollPane = new JScrollPane(tarifTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        tarifInfoPanel.add(scrollPane, BorderLayout.CENTER);

        return tarifInfoPanel;
    }

    private void calculateFee() {
        try {
            String vehicleType = (String) jenisKendaraanCombo.getSelectedItem();
            int hours = (Integer) jamSpinner.getValue();
            int minutes = (Integer) menitSpinner.getValue();

            // Convert to total minutes
            int totalMinutes = (hours * 60) + minutes;

            // Calculate fee
            double fee = calculateParkingFee(vehicleType, totalMinutes);

            // Update display
            DecimalFormat df = new DecimalFormat("#,###");
            totalBiayaLabel.setText("Rp " + df.format(fee));

            // Update detail
            updateDetailBiaya(vehicleType, hours, minutes, totalMinutes, fee);

            LOGGER.info("Tarif calculated - Type: " + vehicleType +
                    ", Duration: " + hours + "h " + minutes + "m, Fee: " + fee);

        } catch (Exception e) {
            LOGGER.severe("Error calculating parking fee: " + e.getMessage());
            totalBiayaLabel.setText("Error");
            detailBiayaArea.setText("Terjadi kesalahan dalam perhitungan tarif.");
        }
    }

    private double calculateParkingFee(String vehicleType, int totalMinutes) {
        // Convert minutes to hours (round up for billing)
        int billingHours = (int) Math.ceil(totalMinutes / 60.0);

        // Minimum 1 hour
        if (billingHours < 1) billingHours = 1;

        double fee = 0;

        switch (vehicleType.toUpperCase()) {
            case "MOTOR":
                fee = 2000; // First hour
                if (billingHours > 1) {
                    fee += (billingHours - 1) * 1000; // Additional hours
                }
                break;
            case "MOBIL":
                fee = 5000; // First hour
                if (billingHours > 1) {
                    fee += (billingHours - 1) * 2000; // Additional hours
                }
                break;
            case "TRUK":
                fee = 10000; // First hour
                if (billingHours > 1) {
                    fee += (billingHours - 1) * 5000; // Additional hours
                }
                break;
            default:
                // Default to mobil rates
                fee = 5000;
                if (billingHours > 1) {
                    fee += (billingHours - 1) * 2000;
                }
                break;
        }

        return fee;
    }

    private void updateDetailBiaya(String vehicleType, int hours, int minutes, int totalMinutes, double fee) {
        StringBuilder detail = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#,###");

        detail.append("DETAIL PERHITUNGAN TARIF\n");
        detail.append("═══════════════════════════\n\n");

        detail.append("Jenis Kendaraan: ").append(vehicleType).append("\n");
        detail.append("Durasi Input: ").append(hours).append(" jam ").append(minutes).append(" menit\n");
        detail.append("Total Menit: ").append(totalMinutes).append(" menit\n\n");

        // Calculate billing hours
        int billingHours = (int) Math.ceil(totalMinutes / 60.0);
        if (billingHours < 1) billingHours = 1;

        detail.append("Jam Tagihan: ").append(billingHours).append(" jam\n");
        detail.append("(Pembulatan ke atas)\n\n");

        // Breakdown calculation
        detail.append("RINCIAN BIAYA:\n");
        detail.append("────────────────────\n");

        switch (vehicleType.toUpperCase()) {
            case "MOTOR":
                detail.append("• Jam pertama: Rp ").append(df.format(2000)).append("\n");
                if (billingHours > 1) {
                    int additionalHours = billingHours - 1;
                    double additionalFee = additionalHours * 1000;
                    detail.append("• ").append(additionalHours).append(" jam berikutnya: ")
                            .append(additionalHours).append(" × Rp 1.000 = Rp ")
                            .append(df.format(additionalFee)).append("\n");
                }
                break;

            case "MOBIL":
                detail.append("• Jam pertama: Rp ").append(df.format(5000)).append("\n");
                if (billingHours > 1) {
                    int additionalHours = billingHours - 1;
                    double additionalFee = additionalHours * 2000;
                    detail.append("• ").append(additionalHours).append(" jam berikutnya: ")
                            .append(additionalHours).append(" × Rp 2.000 = Rp ")
                            .append(df.format(additionalFee)).append("\n");
                }
                break;

            case "TRUK":
                detail.append("• Jam pertama: Rp ").append(df.format(10000)).append("\n");
                if (billingHours > 1) {
                    int additionalHours = billingHours - 1;
                    double additionalFee = additionalHours * 5000;
                    detail.append("• ").append(additionalHours).append(" jam berikutnya: ")
                            .append(additionalHours).append(" × Rp 5.000 = Rp ")
                            .append(df.format(additionalFee)).append("\n");
                }
                break;
        }

        detail.append("\n────────────────────\n");
        detail.append("TOTAL BIAYA: Rp ").append(df.format(fee)).append("\n");
        detail.append("════════════════════════════\n\n");

        detail.append("Catatan:\n");
        detail.append("- Tarif dihitung per jam dengan pembulatan ke atas\n");
        detail.append("- Minimal parkir dikenakan tarif 1 jam\n");
        detail.append("- Menit lebih dari 30 dihitung 1 jam penuh");

        detailBiayaArea.setText(detail.toString());
        detailBiayaArea.setCaretPosition(0); // Scroll to top
    }

    private void updateTarifInfo() {
        // This method can be used to highlight current vehicle type rates
        // in the tariff info panel if needed
    }

    private void resetForm() {
        jenisKendaraanCombo.setSelectedIndex(0);
        jamSpinner.setValue(1);
        menitSpinner.setValue(0);
        calculateFee();
        LOGGER.info("Tarif calculator form reset");
    }

    private void openVerifikasiPlat() {
        try {
            VerifikasiPlat verifikasiPlat = new VerifikasiPlat();
            verifikasiPlat.setVisible(true);
            LOGGER.info("Verifikasi plat window opened from tarif calculator");
        } catch (Exception e) {
            LOGGER.severe("Failed to open verifikasi plat window: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Gagal membuka window verifikasi plat: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openParkirManual() {
        try {
            ParkirManual parkirManual = new ParkirManual();
            parkirManual.setVisible(true);
            LOGGER.info("Parkir manual window opened from tarif calculator");
        } catch (Exception e) {
            LOGGER.severe("Failed to open parkir manual window: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Gagal membuka window parkir manual: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}