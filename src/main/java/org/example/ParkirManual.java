
package org.example;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ParkirManual extends JFrame implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(ParkirManual.class.getName());

    private JTextField platNomorField;
    private JComboBox<String> jenisKendaraanCombo;
    private JTextArea catatanArea;
    private JButton simpanButton;
    private JButton resetButton;
    private JButton tutupButton;

    private Color accentColor = new Color(0, 122, 204);

    public ParkirManual() {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        setTitle("Smart Parking - Input Manual");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        initComponents();
        LOGGER.info("Manual parking input window opened");
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(560, 60));

        JLabel titleLabel = new JLabel("Input Data Kendaraan Manual");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Data Kendaraan",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Plat Nomor
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel platLabel = new JLabel("Plat Nomor:");
        platLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(platLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        platNomorField = new JTextField();
        platNomorField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        platNomorField.setPreferredSize(new Dimension(300, 35));
        platNomorField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(platNomorField, gbc);

        // Jenis Kendaraan
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel jenisLabel = new JLabel("Jenis Kendaraan:");
        jenisLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(jenisLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] jenisKendaraan = {"MOTOR", "MOBIL", "TRUK"};
        jenisKendaraanCombo = new JComboBox<>(jenisKendaraan);
        jenisKendaraanCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jenisKendaraanCombo.setPreferredSize(new Dimension(300, 35));
        jenisKendaraanCombo.setBackground(Color.WHITE);
        formPanel.add(jenisKendaraanCombo, gbc);

        // Catatan
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel catatanLabel = new JLabel("Catatan:");
        catatanLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        catatanLabel.setVerticalAlignment(JLabel.TOP);
        formPanel.add(catatanLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        catatanArea = new JTextArea(4, 30);
        catatanArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        catatanArea.setLineWrap(true);
        catatanArea.setWrapStyleWord(true);
        catatanArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JScrollPane catatanScrollPane = new JScrollPane(catatanArea);
        catatanScrollPane.setPreferredSize(new Dimension(300, 100));
        catatanScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formPanel.add(catatanScrollPane, gbc);

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(245, 245, 245));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(76, 175, 80)),
                "Informasi",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(76, 175, 80)
        ));

        JTextArea infoArea = new JTextArea();
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoArea.setBackground(new Color(245, 245, 245));
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentTime = dateFormat.format(new Date());

        String infoText = "✓ Data kendaraan akan disimpan dengan waktu masuk: " + currentTime + "\n" +
                "✓ Status kendaraan akan diset sebagai ACTIVE\n" +
                "✓ Pastikan plat nomor sudah benar sebelum menyimpan\n" +
                "✓ Catatan bersifat opsional untuk informasi tambahan";

        infoArea.setText(infoText);
        infoPanel.add(infoArea, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        simpanButton = new JButton("Simpan Data");
        simpanButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        simpanButton.setBackground(new Color(76, 175, 80));
        simpanButton.setForeground(Color.WHITE);
        simpanButton.setPreferredSize(new Dimension(130, 40));
        simpanButton.setFocusPainted(false);
        simpanButton.addActionListener(this);

        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setBackground(new Color(158, 158, 158));
        resetButton.setForeground(Color.WHITE);
        resetButton.setPreferredSize(new Dimension(100, 40));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(this);

        tutupButton = new JButton("Tutup");
        tutupButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tutupButton.setBackground(new Color(244, 67, 54));
        tutupButton.setForeground(Color.WHITE);
        tutupButton.setPreferredSize(new Dimension(100, 40));
        tutupButton.setFocusPainted(false);
        tutupButton.addActionListener(this);

        buttonPanel.add(simpanButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(tutupButton);

        // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        // Add button panel to the bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Focus on plat nomor field
        platNomorField.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == simpanButton) {
            simpanData();
        } else if (e.getSource() == resetButton) {
            resetForm();
        } else if (e.getSource() == tutupButton) {
            dispose();
        }
    }

    private void simpanData() {
        // Validasi input
        if (!validateInput()) {
            return;
        }

        String plateNumber = platNomorField.getText().trim().toUpperCase();
        String vehicleType = (String) jenisKendaraanCombo.getSelectedItem();
        String notes = catatanArea.getText().trim();

        try {
            // Gunakan method dari DatabaseConnection untuk logging yang konsisten
            int generatedId = DatabaseConnection.saveVehicleEntry(plateNumber, vehicleType, notes);

            if (generatedId > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String successMessage = "Data kendaraan berhasil disimpan!\n\n" +
                        "ID: " + generatedId + "\n" +
                        "Plat Nomor: " + plateNumber + "\n" +
                        "Jenis: " + vehicleType + "\n" +
                        "Waktu Masuk: " + dateFormat.format(new Date()) + "\n" +
                        "Status: ACTIVE";

                if (!notes.isEmpty()) {
                    successMessage += "\nCatatan: " + notes;
                }

                JOptionPane.showMessageDialog(this,
                        successMessage,
                        "Simpan Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);

                resetForm();
                LOGGER.info("Manual parking entry saved successfully - ID: " + generatedId +
                        ", Plate: " + plateNumber + ", Type: " + vehicleType);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan data kendaraan!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Failed to save manual parking entry for plate: " + plateNumber);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Database error while saving manual parking entry: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        String plateNumber = platNomorField.getText().trim();

        // Validasi plat nomor tidak kosong
        if (plateNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Plat nomor tidak boleh kosong!",
                    "Validasi Error",
                    JOptionPane.WARNING_MESSAGE);
            platNomorField.requestFocus();
            return false;
        }

        // Validasi panjang plat nomor
        if (plateNumber.length() < 3 || plateNumber.length() > 12) {
            JOptionPane.showMessageDialog(this,
                    "Panjang plat nomor tidak valid!\nPanjang plat nomor harus antara 3-12 karakter.",
                    "Validasi Error",
                    JOptionPane.WARNING_MESSAGE);
            platNomorField.requestFocus();
            return false;
        }

        // Validasi format plat nomor (huruf dan angka)
        if (!plateNumber.matches("^[A-Za-z0-9\\s]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Format plat nomor tidak valid!\nPlat nomor hanya boleh mengandung huruf, angka, dan spasi.",
                    "Validasi Error",
                    JOptionPane.WARNING_MESSAGE);
            platNomorField.requestFocus();
            return false;
        }

        // Validasi jenis kendaraan dipilih
        if (jenisKendaraanCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Silakan pilih jenis kendaraan!",
                    "Validasi Error",
                    JOptionPane.WARNING_MESSAGE);
            jenisKendaraanCombo.requestFocus();
            return false;
        }

        return true;
    }

    private void resetForm() {
        platNomorField.setText("");
        jenisKendaraanCombo.setSelectedIndex(0);
        catatanArea.setText("");
        platNomorField.requestFocus();
        LOGGER.info("Manual parking form reset");
    }

    // Getter methods untuk digunakan oleh class lain
    public String getPlatNomor() {
        return platNomorField.getText().trim();
    }

    public String getJenisKendaraan() {
        return (String) jenisKendaraanCombo.getSelectedItem();
    }

    public String getCatatan() {
        return catatanArea.getText().trim();
    }
}