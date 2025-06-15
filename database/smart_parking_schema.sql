-- Membuat database jika belum ada
CREATE DATABASE IF NOT EXISTS smart_parking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Membuat user untuk koneksi database
CREATE USER IF NOT EXISTS 'parking_admin'@'localhost' IDENTIFIED BY 'parkingadmin123';

-- Memberikan hak akses penuh ke user pada database smart_parking
GRANT ALL PRIVILEGES ON smart_parking.* TO 'parking_admin'@'localhost';
FLUSH PRIVILEGES;

-- Menggunakan database smart_parking
USE smart_parking;

-- Membuat tabel parking_entries (UPDATE dengan kolom scan data)
CREATE TABLE IF NOT EXISTS parking_entries (
                                               id INT AUTO_INCREMENT PRIMARY KEY,
                                               plate_number VARCHAR(20) NOT NULL,
    vehicle_type ENUM('Motor', 'Mobil', 'Truk') NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    status ENUM('ACTIVE', 'COMPLETED') NOT NULL,
    duration_minutes INT NULL,
    fee DECIMAL(10, 2) NULL,
    notes TEXT NULL,
    -- Kolom baru untuk data scan
    original_image_path VARCHAR(500) NULL,
    processed_image_path VARCHAR(500) NULL,
    ocr_raw_text TEXT NULL,
    scan_method ENUM('MANUAL', 'OCR_SCAN') DEFAULT 'MANUAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plate_number (plate_number),
    INDEX idx_status (status),
    INDEX idx_entry_time (entry_time),
    INDEX idx_exit_time (exit_time),
    INDEX idx_scan_method (scan_method)
    ) ENGINE=InnoDB;

-- Membuat tabel scan_sessions (untuk tracking semua percobaan scan)
CREATE TABLE IF NOT EXISTS scan_sessions (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             plate_number VARCHAR(20) NULL,
    original_image_path VARCHAR(500) NOT NULL,
    processed_image_path VARCHAR(500) NULL,
    ocr_raw_text TEXT NULL,
    ocr_cleaned_text TEXT NULL,
    scan_success BOOLEAN DEFAULT FALSE,
    scan_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processing_time_ms INT NULL,
    INDEX idx_scan_timestamp (scan_timestamp),
    INDEX idx_scan_success (scan_success),
    INDEX idx_plate_number (plate_number)
    ) ENGINE=InnoDB;

-- Membuat tabel parking_rates (untuk konfigurasi tarif)
CREATE TABLE IF NOT EXISTS parking_rates (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             vehicle_type ENUM('Motor', 'Mobil', 'Truk') NOT NULL,
    hourly_rate DECIMAL(10, 2) NOT NULL,
    first_hour_rate DECIMAL(10, 2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_vehicle_type (vehicle_type, is_active)
    ) ENGINE=InnoDB;

-- Mengisi data awal untuk tarif parkir
INSERT INTO parking_rates (vehicle_type, hourly_rate, first_hour_rate, is_active)
VALUES
    ('Motor', 2000.00, 2000.00, TRUE),
    ('Mobil', 5000.00, 5000.00, TRUE),
    ('Truk', 10000.00, 10000.00, TRUE)
    ON DUPLICATE KEY UPDATE
                         hourly_rate = VALUES(hourly_rate),
                         first_hour_rate = VALUES(first_hour_rate);

-- Membuat tabel users (untuk login admin)
CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('admin', 'operator') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_username (username)
    ) ENGINE=InnoDB;

-- Mengisi data default admin (password: admin123)
INSERT INTO users (username, password, full_name, role)
VALUES ('admin', 'admin123', 'Administrator', 'admin')
    ON DUPLICATE KEY UPDATE
                         password = VALUES(password),
                         full_name = VALUES(full_name);

-- Mengisi data default operator (password: operator123)
INSERT INTO users (username, password, full_name, role)
VALUES ('operator', 'operator123', 'Operator Parkir', 'operator')
    ON DUPLICATE KEY UPDATE
                         password = VALUES(password),
                         full_name = VALUES(full_name);

-- Menampilkan info setup
SELECT 'Database smart_parking berhasil dibuat!' as status;
SELECT 'User parking_admin berhasil dibuat!' as status;
SELECT COUNT(*) as total_tables, 'tabel berhasil dibuat' as status FROM information_schema.tables WHERE table_schema = 'smart_parking';