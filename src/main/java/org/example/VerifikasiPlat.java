
package org.example;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VerifikasiPlat extends JFrame {

    private JTextField searchField;
    private JPanel resultPanel;
    private JPanel noResultPanel;
    private JLabel platValue;
    private JLabel waktuMasukValue;
    private JLabel tanggalMasukValue;
    private JLabel statusValue;
    private JLabel durasiValue;
    private JButton prosesKeluarButton;
    
    private Color accentColor = new Color(0, 122, 204);
    private boolean resultFound = false;

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

        JLabel searchLabel = new JLabel("Plat Nomor:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(200, 35));

        JButton searchButton = new JButton("Verifikasi");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(accentColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verifyPlate();
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Result panel
        resultPanel = new JPanel(new BorderLayout(0, 10));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        resultPanel.setVisible(false);

        JPanel resultHeader = new JPanel(new BorderLayout());
        resultHeader.setBackground(new Color(240, 240, 240));
        resultHeader.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel resultHeaderLabel = new JLabel("Informasi Kendaraan");
        resultHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultHeader.add(resultHeaderLabel, BorderLayout.WEST);

        JPanel resultContent = new JPanel(new GridLayout(5, 2, 10, 15));
        resultContent.setBackground(Color.WHITE);
        resultContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Detail labels
        JLabel platLabel = new JLabel("Plat Nomor:");
        platLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        platValue = new JLabel("-");
        platValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel waktuMasukLabel = new JLabel("Waktu Masuk:");
        waktuMasukLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        waktuMasukValue = new JLabel("-");
        waktuMasukValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel tanggalMasukLabel = new JLabel("Tanggal Masuk:");
        tanggalMasukLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tanggalMasukValue = new JLabel("-");
        tanggalMasukValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusValue = new JLabel("-");
        statusValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel durasiLabel = new JLabel("Durasi:");
        durasiLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        durasiValue = new JLabel("-");
        durasiValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        resultContent.add(platLabel);
        resultContent.add(platValue);
        resultContent.add(waktuMasukLabel);
        resultContent.add(waktuMasukValue);
        resultContent.add(tanggalMasukLabel);
        resultContent.add(tanggalMasukValue);
        resultContent.add(statusLabel);
        resultContent.add(statusValue);
        resultContent.add(durasiLabel);
        resultContent.add(durasiValue);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(new EmptyBorder(0, 20, 10, 20));

        prosesKeluarButton = new JButton("Proses Keluar");
        prosesKeluarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        prosesKeluarButton.setBackground(new Color(204, 0, 0));
        prosesKeluarButton.setForeground(Color.WHITE);
        prosesKeluarButton.setFocusPainted(false);
        prosesKeluarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processExit();
            }
        });

        actionPanel.add(prosesKeluarButton);

        resultPanel.add(resultHeader, BorderLayout.NORTH);
        resultPanel.add(resultContent, BorderLayout.CENTER);
        resultPanel.add(actionPanel, BorderLayout.SOUTH);

        // No Result Panel
        noResultPanel = new JPanel(new BorderLayout());
        noResultPanel.setBackground(new Color(255, 248, 220));
        noResultPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        noResultPanel.setVisible(false);

        JLabel noResultLabel = new JLabel("Kendaraan tidak ditemukan!");
        noResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        noResultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noResultLabel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerPanel.setBackground(new Color(255, 248, 220));
        registerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton registerButton = new JButton("Daftarkan Kendaraan Baru");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setBackground(new Color(0, 153, 51));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerNewVehicle();
            }
        });

        registerPanel.add(registerButton);

        noResultPanel.add(noResultLabel, BorderLayout.CENTER);
        noResultPanel.add(registerPanel, BorderLayout.SOUTH);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(resultPanel, BorderLayout.CENTER);
        contentPanel.add(noResultPanel, BorderLayout.CENTER);

        // Add all components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);
    }

    private void verifyPlate() {
        String platNomor = searchField.getText().trim();
        
        if (platNomor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan masukkan plat nomor!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // In a real application, this would query the database
        // For demonstration, let's just show a result if the plate is "B 1234 CD"
        if ("B 1234 CD".equals(platNomor)) {
            resultFound = true;
            
            // Set the values
            platValue.setText(platNomor);
            waktuMasukValue.setText("13:45:30");
            tanggalMasukValue.setText("12-06-2025");
            statusValue.setText("Parkir Aktif");
            statusValue.setForeground(new Color(0, 153, 51));
            durasiValue.setText("1 jam 24 menit");
            
            // Show the result panel
            resultPanel.setVisible(true);
            noResultPanel.setVisible(false);
        } else {
            resultFound = false;
            
            // Show the no result panel
            resultPanel.setVisible(false);
            noResultPanel.setVisible(true);
        }
    }

    private void processExit() {
        if (!resultFound) {
            return;
        }
        
        String platNomor = platValue.getText();
        
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Proses keluar kendaraan dengan plat nomor " + platNomor + "?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // In a real application, this would update the database
            JOptionPane.showMessageDialog(this, 
                    "Kendaraan dengan plat nomor " + platNomor + " berhasil keluar!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Reset the form
            searchField.setText("");
            resultPanel.setVisible(false);
            noResultPanel.setVisible(false);
            resultFound = false;
        }
    }

    private void registerNewVehicle() {
        String platNomor = searchField.getText().trim();
        
        if (platNomor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan masukkan plat nomor terlebih dahulu!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Daftarkan kendaraan baru dengan plat nomor " + platNomor + "?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // In a real application, this would add to the database
            JOptionPane.showMessageDialog(this, 
                    "Kendaraan dengan plat nomor " + platNomor + " berhasil didaftarkan!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Reset the form
            searchField.setText("");
            resultPanel.setVisible(false);
            noResultPanel.setVisible(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VerifikasiPlat().setVisible(true);
            }
        });
    }
}