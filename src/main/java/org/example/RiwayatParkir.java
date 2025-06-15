package org.example;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class RiwayatParkir extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(RiwayatParkir.class.getName());

    private JTable riwayatTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> jenisKendaraanFilterCombo;
    private JTextField searchField;
    private JLabel totalRecordsLabel;
    private TableRowSorter<DefaultTableModel> sorter;

    private Color accentColor = new Color(0, 122, 204);

    public RiwayatParkir() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        setTitle("Smart Parking - Riwayat Parkir");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        initComponents();
        loadRiwayatData();
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(1200, 60));

        JLabel titleLabel = new JLabel("Riwayat Parkir Kendaraan");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Filter & Pencarian",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        // Search field
        JLabel searchLabel = new JLabel("Cari Plat:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(150, 30));

        // Status filter
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] statusOptions = {"Semua", "ACTIVE", "COMPLETED"};
        statusFilterCombo = new JComboBox<>(statusOptions);
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilterCombo.setPreferredSize(new Dimension(120, 30));

        // Jenis kendaraan filter
        JLabel jenisLabel = new JLabel("Jenis:");
        jenisLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] jenisOptions = {"Semua", "MOTOR", "MOBIL", "TRUK"};
        jenisKendaraanFilterCombo = new JComboBox<>(jenisOptions);
        jenisKendaraanFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jenisKendaraanFilterCombo.setPreferredSize(new Dimension(120, 30));

        // Filter button
        JButton filterButton = new JButton("Filter");
        filterButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterButton.setBackground(accentColor);
        filterButton.setForeground(Color.WHITE);
        filterButton.setPreferredSize(new Dimension(80, 30));
        filterButton.setFocusPainted(false);
        filterButton.addActionListener(e -> applyFilter());

        // Reset filter button
        JButton resetFilterButton = new JButton("Reset");
        resetFilterButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resetFilterButton.setBackground(new Color(158, 158, 158));
        resetFilterButton.setForeground(Color.WHITE);
        resetFilterButton.setPreferredSize(new Dimension(80, 30));
        resetFilterButton.setFocusPainted(false);
        resetFilterButton.addActionListener(e -> resetFilter());

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilterCombo);
        filterPanel.add(jenisLabel);
        filterPanel.add(jenisKendaraanFilterCombo);
        filterPanel.add(filterButton);
        filterPanel.add(resetFilterButton);

        // Table
        String[] columnNames = {
                "ID", "Plat Nomor", "Jenis", "Waktu Masuk", "Waktu Keluar",
                "Durasi (Menit)", "Biaya", "Status", "Catatan"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        riwayatTable = new JTable(tableModel);
        riwayatTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        riwayatTable.setRowHeight(25);
        riwayatTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        riwayatTable.getTableHeader().setBackground(new Color(240, 240, 240));
        riwayatTable.setSelectionBackground(new Color(184, 207, 229));

        // Set column widths
        riwayatTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        riwayatTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Plat Nomor
        riwayatTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Jenis
        riwayatTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Waktu Masuk
        riwayatTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Waktu Keluar
        riwayatTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Durasi
        riwayatTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Biaya
        riwayatTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Status
        riwayatTable.getColumnModel().getColumn(8).setPreferredWidth(200); // Catatan

        // Add sorter to table
        sorter = new TableRowSorter<>(tableModel);
        riwayatTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(riwayatTable);
        scrollPane.setPreferredSize(new Dimension(1150, 400));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setBackground(new Color(76, 175, 80));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> {
            loadRiwayatData();
            LOGGER.info("Riwayat parkir data refreshed successfully");
        });

        JButton editButton = new JButton("Edit Data");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editButton.setBackground(new Color(255, 193, 7));
        editButton.setForeground(Color.WHITE);
        editButton.setPreferredSize(new Dimension(100, 35));
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> editSelectedData());

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setPreferredSize(new Dimension(80, 35));
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> deleteSelectedData());

        JButton printButton = new JButton("Print");
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        printButton.setBackground(new Color(255, 152, 0));
        printButton.setForeground(Color.WHITE);
        printButton.setPreferredSize(new Dimension(80, 35));
        printButton.setFocusPainted(false);
        printButton.addActionListener(e -> printTable());

        JButton exportButton = new JButton("Export CSV");
        exportButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportButton.setBackground(new Color(96, 125, 139));
        exportButton.setForeground(Color.WHITE);
        exportButton.setPreferredSize(new Dimension(100, 35));
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportToCSV());

        JButton tutupButton = new JButton("Tutup");
        tutupButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tutupButton.setBackground(new Color(244, 67, 54));
        tutupButton.setForeground(Color.WHITE);
        tutupButton.setPreferredSize(new Dimension(80, 35));
        tutupButton.setFocusPainted(false);
        tutupButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(printButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(tutupButton);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.WHITE);
        totalRecordsLabel = new JLabel("Total Records: 0");
        totalRecordsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalRecordsLabel.setForeground(accentColor);
        statusPanel.add(totalRecordsLabel);

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadRiwayatData() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT id, plate_number, vehicle_type, entry_time, exit_time, " +
                    "duration_minutes, fee, status, notes " +
                    "FROM parking_entries " +
                    "ORDER BY entry_time DESC";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                // Clear existing data
                tableModel.setRowCount(0);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                DecimalFormat decimalFormat = new DecimalFormat("#,###");

                int totalRecords = 0;

                while (rs.next()) {
                    Object[] row = new Object[9];
                    row[0] = rs.getInt("id");
                    row[1] = rs.getString("plate_number");
                    row[2] = rs.getString("vehicle_type");

                    // Format waktu masuk
                    Timestamp entryTime = rs.getTimestamp("entry_time");
                    row[3] = entryTime != null ? dateFormat.format(entryTime) : "-";

                    // Format waktu keluar
                    Timestamp exitTime = rs.getTimestamp("exit_time");
                    row[4] = exitTime != null ? dateFormat.format(exitTime) : "-";

                    // Durasi
                    Long duration = rs.getLong("duration_minutes");
                    if (rs.wasNull()) {
                        row[5] = "-";
                    } else {
                        row[5] = duration;
                    }

                    // Biaya
                    Double fee = rs.getDouble("fee");
                    if (rs.wasNull()) {
                        row[6] = "-";
                    } else {
                        row[6] = "Rp " + decimalFormat.format(fee);
                    }

                    row[7] = rs.getString("status");
                    row[8] = rs.getString("notes") != null ? rs.getString("notes") : "-";

                    tableModel.addRow(row);
                    totalRecords++;
                }

                totalRecordsLabel.setText("Total Records: " + totalRecords);
                LOGGER.info("Loaded " + totalRecords + " parking records successfully");

            }

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Failed to load parking records: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedData() {
        int selectedRow = riwayatTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Silakan pilih data yang ingin diedit!",
                    "Tidak Ada Data Dipilih",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view row index to model row index
        int modelRow = riwayatTable.convertRowIndexToModel(selectedRow);

        int id = (Integer) tableModel.getValueAt(modelRow, 0);
        String plateNumber = (String) tableModel.getValueAt(modelRow, 1);
        String vehicleType = (String) tableModel.getValueAt(modelRow, 2);
        String status = (String) tableModel.getValueAt(modelRow, 7);
        String notes = (String) tableModel.getValueAt(modelRow, 8);

        if ("-".equals(notes)) {
            notes = "";
        }

        // Create edit dialog
        JDialog editDialog = new JDialog(this, "Edit Data Kendaraan", true);
        editDialog.setSize(500, 400);
        editDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // ID (readonly)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField idField = new JTextField(String.valueOf(id));
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        panel.add(idField, gbc);

        // Plat Nomor
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Plat Nomor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField plateField = new JTextField(plateNumber);
        panel.add(plateField, gbc);

        // Jenis Kendaraan
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Jenis Kendaraan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] types = {"MOTOR", "MOBIL", "TRUK"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setSelectedItem(vehicleType);
        panel.add(typeCombo, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] statuses = {"ACTIVE", "COMPLETED"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(status);
        panel.add(statusCombo, gbc);

        // Catatan
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Catatan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        JTextArea notesArea = new JTextArea(notes, 4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        panel.add(notesScroll, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0; gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Simpan");
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> {
            updateData(id, plateField.getText().trim().toUpperCase(),
                    (String) typeCombo.getSelectedItem(),
                    (String) statusCombo.getSelectedItem(),
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

    private void updateData(int id, String plateNumber, String vehicleType, String status, String notes) {
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
                    "plate_number = ?, vehicle_type = ?, status = ?, notes = ? " +
                    "WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setString(1, plateNumber);
                stmt.setString(2, vehicleType);
                stmt.setString(3, status);
                stmt.setString(4, notes.isEmpty() ? null : notes);
                stmt.setInt(5, id);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Data berhasil diupdate!",
                            "Update Berhasil",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRiwayatData(); // Refresh data
                    LOGGER.info("Vehicle data updated successfully for ID: " + id);
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

    private void deleteSelectedData() {
        int selectedRow = riwayatTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Silakan pilih data yang ingin dihapus!",
                    "Tidak Ada Data Dipilih",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view row index to model row index
        int modelRow = riwayatTable.convertRowIndexToModel(selectedRow);

        int id = (Integer) tableModel.getValueAt(modelRow, 0);
        String plateNumber = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data kendaraan:\n" +
                        "ID: " + id + "\n" +
                        "Plat Nomor: " + plateNumber + "\n\n" +
                        "Data yang dihapus tidak dapat dikembalikan!",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();

                String deleteQuery = "DELETE FROM parking_entries WHERE id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, id);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Data berhasil dihapus!",
                                "Hapus Berhasil",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadRiwayatData(); // Refresh data
                        LOGGER.info("Vehicle data deleted successfully for ID: " + id + ", Plate: " + plateNumber);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Gagal menghapus data!",
                                "Hapus Gagal",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.severe("Failed to delete vehicle data: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Error database: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyFilter() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        String jenisFilter = (String) jenisKendaraanFilterCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> combinedFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Search filter (plat nomor)
                if (!searchText.isEmpty()) {
                    String platNomor = entry.getStringValue(1).toLowerCase();
                    if (!platNomor.contains(searchText)) {
                        return false;
                    }
                }

                // Status filter
                if (!"Semua".equals(statusFilter)) {
                    String status = entry.getStringValue(7);
                    if (!statusFilter.equals(status)) {
                        return false;
                    }
                }

                // Jenis kendaraan filter
                if (!"Semua".equals(jenisFilter)) {
                    String jenis = entry.getStringValue(2);
                    if (!jenisFilter.equals(jenis)) {
                        return false;
                    }
                }

                return true;
            }
        };

        sorter.setRowFilter(combinedFilter);

        // Update total records after filter
        int visibleRows = riwayatTable.getRowCount();
        totalRecordsLabel.setText("Total Records: " + visibleRows + " (filtered)");
    }

    private void resetFilter() {
        searchField.setText("");
        statusFilterCombo.setSelectedIndex(0);
        jenisKendaraanFilterCombo.setSelectedIndex(0);
        sorter.setRowFilter(null);

        // Reset total records count
        int totalRows = tableModel.getRowCount();
        totalRecordsLabel.setText("Total Records: " + totalRows);
    }

    private void printTable() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(new TablePrintable());

            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "Data berhasil dicetak!",
                        "Print Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saat mencetak: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class TablePrintable implements Printable {
        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            // Print title
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2d.drawString("Riwayat Parkir - " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()), 50, 30);

            // Print table headers
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            int yPos = 60;
            int xPos = 10;

            String[] headers = {"ID", "Plat", "Jenis", "Masuk", "Keluar", "Status"};
            for (String header : headers) {
                g2d.drawString(header, xPos, yPos);
                xPos += 80;
            }

            // Print table data
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            yPos += 20;

            for (int i = 0; i < riwayatTable.getRowCount() && yPos < pf.getImageableHeight() - 50; i++) {
                xPos = 10;
                for (int j = 0; j < 6; j++) { // Print only first 6 columns
                    String value = riwayatTable.getValueAt(i, j).toString();
                    if (value.length() > 10) {
                        value = value.substring(0, 10) + "...";
                    }
                    g2d.drawString(value, xPos, yPos);
                    xPos += 80;
                }
                yPos += 15;
            }

            return PAGE_EXISTS;
        }
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });

        String defaultFileName = "riwayat_parkir_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
        fileChooser.setSelectedFile(new File(defaultFileName));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure .csv extension
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (FileWriter writer = new FileWriter(file)) {
                // Write headers
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.append(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");

                // Write data
                for (int i = 0; i < riwayatTable.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = riwayatTable.getValueAt(i, j);
                        String stringValue = value != null ? value.toString() : "";

                        // Escape commas and quotes in CSV
                        if (stringValue.contains(",") || stringValue.contains("\"")) {
                            stringValue = "\"" + stringValue.replace("\"", "\"\"") + "\"";
                        }

                        writer.append(stringValue);
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }

                JOptionPane.showMessageDialog(this,
                        "Data berhasil diekspor ke:\n" + file.getAbsolutePath(),
                        "Export Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error saat mengekspor file: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}