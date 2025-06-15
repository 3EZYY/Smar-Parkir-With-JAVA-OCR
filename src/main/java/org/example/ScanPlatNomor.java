package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_videoio.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;


public class ScanPlatNomor extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ScanPlatNomor.class.getName());
    private OpenCVFrameGrabber grabber;
    private Java2DFrameConverter converter;
    private OpenCVFrameConverter.ToMat converterToMat;
    private CanvasFrame canvasFrame;

    private JTextField resultField;
    private JPanel cameraPreviewPanel;
    private JButton captureButton;
    private JButton processButton;
    private JButton saveButton;
    private JComboBox<String> vehicleTypeCombo;
    private Color accentColor = new Color(0, 122, 204);

    private BufferedImage capturedImage;
    private Mat capturedFrame;
    private boolean cameraActive = false;
    private final String TESSDATA_PATH = "C:\\Program Files\\Tesseract-OCR\\tessdata";
    private String lastOcrRawText = "";
    private String lastCapturedImagePath = "";


    static {
        try {
            System.load("C:\\opencv\\build\\java\\x64\\opencv_java480.dll"); // Gunakan path lengkap ke file x64
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native library load failed: " + e);
        }
    }

    public ScanPlatNomor() {
        try {
            FileHandler fileHandler = new FileHandler("ocr_debug.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize FlatLaf", ex);
        }

        converter = new Java2DFrameConverter();
        converterToMat = new OpenCVFrameConverter.ToMat();

        setTitle("Smart Parking - Scan Plat Nomor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        initComponents();

        // Add window listener to release camera resources when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopCamera();
            }
        });

        LOGGER.info("ScanPlatNomor window initialized");
    }


    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(900, 60));

        JLabel titleLabel = new JLabel("Scan Plat Nomor Kendaraan");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Camera control buttons in header
        JPanel cameraControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cameraControlPanel.setOpaque(false);

        JButton startCameraButton = new JButton("Start Camera");
        startCameraButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startCameraButton.addActionListener(e -> startCamera());

        JButton stopCameraButton = new JButton("Stop Camera");
        stopCameraButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        stopCameraButton.addActionListener(e -> stopCamera());

        cameraControlPanel.add(startCameraButton);
        cameraControlPanel.add(stopCameraButton);
        headerPanel.add(cameraControlPanel, BorderLayout.EAST);

        // Camera preview panel
        cameraPreviewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (capturedImage != null) {
                    // Draw the captured image
                    g.drawImage(capturedImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Draw a placeholder
                    g.setColor(new Color(240, 240, 240));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.GRAY);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    FontMetrics fm = g.getFontMetrics();
                    String message = "Camera Preview";
                    int x = (getWidth() - fm.stringWidth(message)) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g.drawString(message, x, y);
                }
            }
        };
        cameraPreviewPanel.setPreferredSize(new Dimension(640, 380));
        cameraPreviewPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Result panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        resultPanel.setBackground(Color.WHITE);

        // Left panel for plate number
        JPanel platePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        platePanel.setBackground(Color.WHITE);

        JLabel resultLabel = new JLabel("Plat Nomor Terdeteksi:");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        resultField = new JTextField();
        resultField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultField.setPreferredSize(new Dimension(200, 35));

        platePanel.add(resultLabel);
        platePanel.add(resultField);

        // Right panel for vehicle type
        JPanel vehicleTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        vehicleTypePanel.setBackground(Color.WHITE);

        JLabel vehicleTypeLabel = new JLabel("Jenis Kendaraan:");
        vehicleTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] vehicleTypes = {"MOTOR", "MOBIL", "TRUK"};
        vehicleTypeCombo = new JComboBox<>(vehicleTypes);
        vehicleTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        vehicleTypeCombo.setPreferredSize(new Dimension(150, 35));

        vehicleTypePanel.add(vehicleTypeLabel);
        vehicleTypePanel.add(vehicleTypeCombo);

        // Combine plate and vehicle type panels
        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.add(platePanel);
        inputPanel.add(vehicleTypePanel);

        resultPanel.add(inputPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        captureButton = new JButton("Capture");
        captureButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        captureButton.setBackground(accentColor);
        captureButton.setForeground(Color.WHITE);
        captureButton.setPreferredSize(new Dimension(120, 40));
        captureButton.setFocusPainted(false);
        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureImage();
            }
        });

        processButton = new JButton("Process OCR");
        processButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processButton.setBackground(new Color(0, 153, 51));
        processButton.setForeground(Color.WHITE);
        processButton.setPreferredSize(new Dimension(120, 40));
        processButton.setFocusPainted(false);
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processImage();
            }
        });

        saveButton = new JButton("Simpan");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(153, 102, 0));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveVehicleData();
            }
        });

        buttonPanel.add(captureButton);
        buttonPanel.add(processButton);
        buttonPanel.add(saveButton);

        // Help panel
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
        helpPanel.setBackground(new Color(240, 248, 255));
        helpPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Panduan",
                1,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        JTextArea helpText = new JTextArea(
                "1. Klik 'Start Camera' untuk memulai kamera.\n" +
                        "2. Posisikan plat nomor kendaraan di depan kamera.\n" +
                        "3. Klik tombol 'Capture' untuk mengambil gambar.\n" +
                        "4. Klik tombol 'Process OCR' untuk mendeteksi teks.\n" +
                        "5. Edit hasil jika diperlukan dan pilih jenis kendaraan.\n" +
                        "6. Klik tombol 'Simpan' untuk menyimpan data kendaraan."
        );
        helpText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helpText.setEditable(false);
        helpText.setBackground(new Color(240, 248, 255));
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setBorder(new EmptyBorder(10, 10, 10, 10));

        helpPanel.add(helpText);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(cameraPreviewPanel, BorderLayout.CENTER);
        contentPanel.add(helpPanel, BorderLayout.EAST);

        // Add all components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);
    }

    private void startCamera() {
        if (cameraActive) {
            JOptionPane.showMessageDialog(this,
                    "Kamera sudah aktif", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Pastikan tidak ada grabber lama yang masih berjalan
            stopCamera();

            // Coba beberapa opsi untuk membuka kamera
            grabber = new OpenCVFrameGrabber(0);

            // Konfigurasi tambahan untuk MSMF di Windows
            grabber.setFormat("dshow");

            // Coba set properti kamera secara spesifik
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);
            grabber.setFrameRate(30);

            // Menambahkan timeout lebih panjang untuk inisialisasi
            grabber.setTimeout(10000); // 10 detik timeout

            LOGGER.info("Memulai kamera dengan format: dshow");
            grabber.start();

            // Validasi kamera terbuka dengan mencoba grab frame pertama
            Frame testFrame = grabber.grab();
            if (testFrame == null) {
                throw new Exception("Tidak dapat mengakses gambar dari kamera");
            }

            cameraActive = true;

            // Set converter untuk digunakan di updateCameraImage
            converter = new Java2DFrameConverter();
            converterToMat = new OpenCVFrameConverter.ToMat();

            // Start background thread dengan error handling
            ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Camera-Thread");
                t.setDaemon(true); // Thread mati saat aplikasi ditutup
                return t;
            });

            timer.scheduleAtFixedRate(() -> {
                try {
                    updateCameraImage();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in camera thread", e);
                }
            }, 0, 33, TimeUnit.MILLISECONDS);

            JOptionPane.showMessageDialog(this,
                    "Kamera berhasil diaktifkan", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting camera: " + e.getMessage(), e);

            // Coba pendekatan alternatif dengan kamera lain
            try {
                LOGGER.info("Mencoba dengan kamera alternatif (index 1)...");
                grabber = new OpenCVFrameGrabber(1);
                grabber.setFormat("dshow");
                grabber.start();

                // Validasi frame
                Frame altFrame = grabber.grab();
                if (altFrame == null) {
                    throw new Exception("Tidak dapat mengakses gambar dari kamera alternatif");
                }

                cameraActive = true;
                converter = new Java2DFrameConverter();
                converterToMat = new OpenCVFrameConverter.ToMat();

                // Start thread dengan kamera alternatif
                ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
                timer.scheduleAtFixedRate(this::updateCameraImage, 0, 33, TimeUnit.MILLISECONDS);

                JOptionPane.showMessageDialog(this,
                        "Kamera alternatif berhasil diaktifkan", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e2) {
                LOGGER.log(Level.SEVERE, "Error pada semua opsi kamera", e2);
                JOptionPane.showMessageDialog(this,
                        "Tidak dapat menghubungkan ke kamera. Pastikan:\n" +
                                "1. Kamera tidak digunakan aplikasi lain\n" +
                                "2. Driver kamera terpasang dengan benar\n" +
                                "3. Izin akses kamera diberikan untuk aplikasi",
                        "Error Kamera", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateCameraImage() {
        if (!cameraActive || grabber == null) {
            return;
        }

        try {
            Frame frame = grabber.grab();
            if (frame != null) {
                // Convert the frame to an image for display
                BufferedImage currentFrame = converter.convert(frame);

                // Update UI on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    capturedImage = currentFrame;
                    cameraPreviewPanel.repaint();
                });
            } else {
                LOGGER.warning("Frame null dari grabber");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating camera image", e);

            // Tambahkan kode recovery untuk mencoba kembali setelah error
            int retryCount = 0;
            while (cameraActive && retryCount < 3) {
                try {
                    // Tunggu sebentar dan coba lagi
                    Thread.sleep(500);
                    Frame recoveryFrame = grabber.grab();
                    if (recoveryFrame != null) {
                        BufferedImage currentFrame = converter.convert(recoveryFrame);
                        SwingUtilities.invokeLater(() -> {
                            capturedImage = currentFrame;
                            cameraPreviewPanel.repaint();
                        });
                        LOGGER.info("Camera recovered after error");
                        return;
                    }
                    retryCount++;
                } catch (Exception innerEx) {
                    LOGGER.log(Level.WARNING, "Recovery attempt " + retryCount + " failed", innerEx);
                }
            }

            // Jika recovery gagal setelah beberapa percobaan, restart kamera
            if (retryCount >= 3) {
                SwingUtilities.invokeLater(() -> {
                    stopCamera();
                    JOptionPane.showMessageDialog(ScanPlatNomor.this,
                            "Koneksi kamera terputus, silakan start ulang kamera",
                            "Error Kamera", JOptionPane.WARNING_MESSAGE);
                });
            }
        }
    }

    private void stopCamera() {
        cameraActive = false;

        if (grabber != null) {
            try {
                // Release the camera
                grabber.stop();
                grabber.release();
                grabber = null;
                LOGGER.info("Camera stopped successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error stopping camera", e);
            }
        }

        // Clear the preview
        capturedImage = null;
        cameraPreviewPanel.repaint();
    }

    private void captureImage() {
        if (!cameraActive) {
            JOptionPane.showMessageDialog(this,
                    "Kamera tidak aktif. Silakan start kamera terlebih dahulu.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Capture current frame
            Frame frame = grabber.grab();
            if (frame != null) {
                capturedImage = converter.convert(frame);
                capturedFrame = converterToMat.convert(frame);

                // Save the captured image to a file
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "captured_images/plate_" + timestamp + ".jpg";

                // Ensure directory exists
                new File("captured_images").mkdirs();

                imwrite(filename, capturedFrame);

                // Simpan path untuk digunakan nanti
                lastCapturedImagePath = new File(filename).getAbsolutePath();

                LOGGER.info("Captured image saved to: " + filename);

                // Keep the current displayed image
                JOptionPane.showMessageDialog(this,
                        "Gambar berhasil diambil dan disimpan!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal mengambil gambar dari kamera",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error capturing image", e);
            JOptionPane.showMessageDialog(this,
                    "Error saat mengambil gambar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processImage() {
        if (capturedFrame == null || capturedFrame.empty()) {
            JOptionPane.showMessageDialog(this,
                    "Silakan ambil gambar terlebih dahulu!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Simpan gambar original terlebih dahulu untuk OCR fallback
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String originalForOcr = "captured_images/original_for_ocr_" + timestamp + ".jpg";
            imwrite(originalForOcr, capturedFrame);

            // Create a copy of the captured frame for processing
            Mat processedFrame = capturedFrame.clone();

            // 1. Convert to grayscale
            Mat grayFrame = new Mat();
            cvtColor(processedFrame, grayFrame, COLOR_BGR2GRAY);
            debugImageInfo(grayFrame, "Grayscale Image");

            // 2. Apply noise reduction with bilateral filter
            Mat denoisedFrame = new Mat();
            bilateralFilter(grayFrame, denoisedFrame, 11, 17, 17);

            // 3. Apply adaptive thresholding
            Mat thresholdFrame = new Mat();
            adaptiveThreshold(denoisedFrame, thresholdFrame, 255,
                    ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 13, 5);

            // 4. Apply morphological operations
            Mat morphKernel = new Mat(3, 3, CV_8UC1, new Scalar(1));
            Mat cleanedFrame = new Mat();
            morphologyEx(thresholdFrame, cleanedFrame, MORPH_CLOSE, morphKernel);
            morphologyEx(cleanedFrame, cleanedFrame, MORPH_OPEN, morphKernel);

            // Save the processed image
            String processedFilename = "captured_images/processed_" + timestamp + ".jpg";
            imwrite(processedFilename, cleanedFrame);

            // Try OCR on multiple versions of the image
            String recognizedText = "";

            // 1. Try original image first
            LOGGER.info("Trying OCR on original image...");
            recognizedText = performOCR(new File(originalForOcr));

            // 2. If original fails, try grayscale
            if (recognizedText.trim().isEmpty()) {
                LOGGER.info("Original failed, trying grayscale...");
                String grayFilename = "captured_images/gray_" + timestamp + ".jpg";
                imwrite(grayFilename, grayFrame);
                recognizedText = performOCR(new File(grayFilename));
            }

            // 3. If grayscale fails, try processed
            if (recognizedText.trim().isEmpty()) {
                LOGGER.info("Grayscale failed, trying processed...");
                recognizedText = performOCR(new File(processedFilename));
            }

            // Simpan raw text
            lastOcrRawText = recognizedText;
            LOGGER.info("Final OCR result: '" + recognizedText + "'");

            // Clean up the recognized text
            String cleanedText = cleanupLicensePlate(recognizedText);

            // Simpan scan session untuk analisis
            DatabaseConnection.saveScanSession(
                    cleanedText,
                    lastCapturedImagePath,
                    new File(processedFilename).getAbsolutePath(),
                    lastOcrRawText,
                    "OCR processing session",
                    !cleanedText.isEmpty()
            );

            // Set the result
            resultField.setText(cleanedText);

            // Show processed image or original if OCR worked better on original
            if (!recognizedText.trim().isEmpty()) {
                // Show the image that worked
                capturedImage = matToBufferedImage(grayFrame);
            } else {
                capturedImage = matToBufferedImage(cleanedFrame);
            }
            cameraPreviewPanel.repaint();

            if (!cleanedText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "OCR berhasil! Plat nomor terdeteksi: " + cleanedText,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "OCR tidak dapat mendeteksi teks. Coba:\n" +
                                "1. Pastikan plat nomor terlihat jelas\n" +
                                "2. Coba ambil gambar dengan pencahayaan lebih baik\n" +
                                "3. Posisikan plat nomor lebih dekat/tegak lurus",
                        "OCR Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing image", e);
            JOptionPane.showMessageDialog(this,
                    "Error saat memproses gambar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String performOCR(File imageFile) {
        try {
            LOGGER.info("Attempting OCR on file: " + imageFile.getAbsolutePath());

            // Check if file exists and is readable
            if (!imageFile.exists()) {
                LOGGER.severe("OCR file does not exist: " + imageFile.getAbsolutePath());
                return "";
            }

            if (!imageFile.canRead()) {
                LOGGER.severe("OCR file is not readable: " + imageFile.getAbsolutePath());
                return "";
            }

            LOGGER.info("File size: " + imageFile.length() + " bytes");

            Tesseract tesseract = new Tesseract();

            // Check if tessdata path exists
            File tessdataDir = new File(TESSDATA_PATH);
            if (!tessdataDir.exists()) {
                LOGGER.warning("Tessdata directory not found: " + TESSDATA_PATH);
                // Try alternative paths
                String[] altPaths = {
                        System.getProperty("user.dir") + "/tessdata",
                        "tessdata",
                        "C:/Program Files/Tesseract-OCR/tessdata",
                        "C:/Users/" + System.getProperty("user.name") + "/AppData/Local/Tesseract-OCR/tessdata"
                };

                for (String altPath : altPaths) {
                    File altDir = new File(altPath);
                    if (altDir.exists()) {
                        LOGGER.info("Using alternative tessdata path: " + altPath);
                        tesseract.setDatapath(altPath);
                        break;
                    }
                }
            } else {
                tesseract.setDatapath(TESSDATA_PATH);
            }

            // Try multiple language configurations
            String[] languages = {"eng", "ind", "eng+ind"};
            String result = "";

            for (String lang : languages) {
                try {
                    LOGGER.info("Trying OCR with language: " + lang);
                    tesseract.setLanguage(lang);

                    // Configure for license plate recognition
                    tesseract.setPageSegMode(6); // Uniform block of text
                    tesseract.setOcrEngineMode(3); // Default OCR engine mode

                    // Set whitelist for license plate characters
                    tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ");

                    // Additional configuration
                    tesseract.setVariable("preserve_interword_spaces", "1");
                    tesseract.setVariable("user_defined_dpi", "300");

                    result = tesseract.doOCR(imageFile);
                    LOGGER.info("OCR result with " + lang + ": '" + result + "'");

                    if (result != null && !result.trim().isEmpty()) {
                        LOGGER.info("OCR successful with language: " + lang);
                        break;
                    }

                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "OCR failed with language " + lang, e);
                }
            }

            // If still empty, try with different page segmentation modes
            if (result == null || result.trim().isEmpty()) {
                LOGGER.info("Trying alternative page segmentation modes...");
                int[] modes = {7, 8, 13}; // single text line, single word, raw line

                for (int mode : modes) {
                    try {
                        tesseract.setLanguage("eng");
                        tesseract.setPageSegMode(mode);
                        result = tesseract.doOCR(imageFile);
                        LOGGER.info("OCR result with PSM " + mode + ": '" + result + "'");

                        if (result != null && !result.trim().isEmpty()) {
                            LOGGER.info("OCR successful with PSM: " + mode);
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "OCR failed with PSM " + mode, e);
                    }
                }
            }

            return result != null ? result : "";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during OCR", e);
            return "";
        }
    }

    private String cleanupLicensePlate(String text) {
        // Log input dari OCR untuk debugging
        LOGGER.info("Original OCR text: " + text);

        if (text == null || text.isEmpty()) {
            return "";
        }

        // Hapus karakter yang tidak diinginkan dan spasi berlebih
        text = text.replaceAll("[^A-Z0-9 ]", "").trim();
        text = text.replaceAll("\\s+", " ");

        LOGGER.info("After basic cleanup: " + text);

        // Jika teks terlalu pendek, kemungkinan deteksi tidak bagus
        if (text.length() < 3) {
            return text;
        }

        // Untuk format plat Indonesia (B 1234 ABC)
        StringBuilder result = new StringBuilder();
        boolean hasLetters = false;
        boolean hasNumbers = false;

        // Cek apakah ada huruf dan angka
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) hasLetters = true;
            if (Character.isDigit(c)) hasNumbers = true;
        }

        // Jika ada keduanya, coba format sebagai plat nomor
        if (hasLetters && hasNumbers) {
            // Pisahkan bagian-bagian plat
            String[] parts = text.split(" ");

            if (parts.length == 1) {
                // Jika tidak ada spasi, coba pisahkan secara manual
                String plate = parts[0];
                int i = 0;

                // Cari bagian pertama (huruf wilayah)
                while (i < plate.length() && Character.isLetter(plate.charAt(i))) {
                    result.append(plate.charAt(i));
                    i++;
                }

                result.append(" ");

                // Cari bagian angka
                while (i < plate.length() && Character.isDigit(plate.charAt(i))) {
                    result.append(plate.charAt(i));
                    i++;
                }

                // Jika masih ada karakter tersisa (huruf akhir)
                if (i < plate.length()) {
                    result.append(" ");
                    while (i < plate.length()) {
                        result.append(plate.charAt(i));
                        i++;
                    }
                }
            } else {
                // Jika sudah ada spasi, gabungkan kembali dengan format yang benar
                for (String part : parts) {
                    result.append(part).append(" ");
                }
            }
        } else {
            // Jika tidak terdeteksi sebagai plat nomor, kembalikan teks asli
            result.append(text);
        }

        String cleaned = result.toString().trim();
        LOGGER.info("Final cleaned plate: " + cleaned);

        return cleaned;
    }

    private void saveVehicleData() {
        String platNomor = resultField.getText().trim().toUpperCase();

        if (platNomor.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Plat nomor tidak boleh kosong!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String vehicleType = (String) vehicleTypeCombo.getSelectedItem();

        // First check if vehicle is already parked
        try {
            if (DatabaseConnection.isVehicleParked(platNomor)) {
                JOptionPane.showMessageDialog(this,
                        "Kendaraan dengan plat nomor " + platNomor + " sudah terdaftar dan masih dalam area parkir!",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Dapatkan path gambar yang disimpan
            String originalImagePath = null;
            String processedImagePath = null;

            // Cari file gambar terbaru di folder captured_images
            File capturedDir = new File("captured_images");
            if (capturedDir.exists()) {
                File[] files = capturedDir.listFiles();
                if (files != null && files.length > 0) {
                    // Urutkan berdasarkan waktu modifikasi (terbaru dulu)
                    java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

                    for (File file : files) {
                        if (file.getName().startsWith("plate_")) {
                            originalImagePath = file.getAbsolutePath();
                            break;
                        }
                    }

                    for (File file : files) {
                        if (file.getName().startsWith("processed_")) {
                            processedImagePath = file.getAbsolutePath();
                            break;
                        }
                    }
                }
            }

            // Simpan dengan data scan
            int vehicleId = DatabaseConnection.saveVehicleEntryWithScan(
                    platNomor,
                    vehicleType,
                    "Captured via OCR scan",
                    originalImagePath != null ? originalImagePath : "",
                    processedImagePath != null ? processedImagePath : "",
                    lastOcrRawText
            );

            if (vehicleId > 0) {
                JOptionPane.showMessageDialog(this,
                        "Data kendaraan dengan plat nomor " + platNomor + " berhasil disimpan!\n" +
                                "ID: " + vehicleId + "\n" +
                                "Metode: OCR Scan\n" +
                                "Status: ACTIVE",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Reset form
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan data kendaraan. Silakan coba lagi.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving vehicle data", e);
            JOptionPane.showMessageDialog(this,
                    "Error saat menyimpan data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        capturedFrame = null;
        capturedImage = null;
        resultField.setText("");
        vehicleTypeCombo.setSelectedIndex(0);
        lastOcrRawText = "";
        lastCapturedImagePath = "";
        cameraPreviewPanel.repaint();
        LOGGER.info("Scan form reset");
    }

    private void debugImageInfo(Mat image, String description) {
        LOGGER.info("=== " + description + " ===");
        LOGGER.info("Image size: " + image.cols() + "x" + image.rows());
        LOGGER.info("Image channels: " + image.channels());
        LOGGER.info("Image depth: " + image.depth());
        LOGGER.info("Image type: " + image.type());
        LOGGER.info("Is empty: " + image.empty());
    }

    // Convert OpenCV Mat to Java BufferedImage
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        mat.data().get(sourcePixels);

        BufferedImage image = new BufferedImage(width, height,
                channels > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY);

        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }
}