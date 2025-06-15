# Smart Parking System

Sistem manajemen parkir otomatis dengan teknologi pengenalan plat nomor (OCR) menggunakan Java Swing dan OpenCV.

## 📋 Deskripsi

Smart Parking System adalah aplikasi desktop yang memungkinkan pengelolaan area parkir secara otomatis. Sistem ini dilengkapi dengan fitur pengenalan plat nomor kendaraan menggunakan teknologi OCR (Optical Character Recognition), manajemen database, dan berbagai fitur administrasi parkir.

## ✨ Fitur Utama

### 🎯 Core Features
- **Scan Plat Nomor**: Deteksi otomatis plat nomor menggunakan kamera dan OCR
- **Parkir Manual**: Input data kendaraan secara manual
- **Verifikasi Plat**: Pencarian dan verifikasi kendaraan yang akan keluar
- **Riwayat Parkir**: Melihat dan mengelola riwayat kendaraan
- **Cek Tarif**: Kalkulator biaya parkir berdasarkan durasi dan jenis kendaraan

### 🔧 Administrative Features
- **Dashboard Admin**: Monitoring real-time statistik parkir
- **User Management**: Login system dengan role admin/operator
- **Database Management**: Pengelolaan data kendaraan dan transaksi
- **Export/Import**: Export data ke CSV dan print laporan

### 💰 Sistem Tarif
- **Motor**: Rp 2.000 (jam pertama) + Rp 1.000 (per jam berikutnya)
- **Mobil**: Rp 5.000 (jam pertama) + Rp 2.000 (per jam berikutnya)
- **Truk**: Rp 10.000 (jam pertama) + Rp 5.000 (per jam berikutnya)

## 🛠️ Teknologi yang Digunakan

- **Language**: Java 21
- **GUI Framework**: Java Swing
- **Look and Feel**: FlatLaf (Modern UI)
- **Computer Vision**: OpenCV 4.8.0
- **OCR Engine**: Tesseract 5.8.0
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Dependencies**:
  - JavaCV Platform 1.5.10
  - Tess4J 5.8.0
  - MySQL Connector 8.0.33
  - SLF4J Logging

## 📁 Struktur Proyek

```
Smart-Parkir/
├── src/main/java/org/example/
│   ├── AdminDashboard.java      # Dashboard utama admin
│   ├── ScanPlatNomor.java       # Modul scan OCR
│   ├── VerifikasiPlat.java      # Verifikasi kendaraan keluar
│   ├── ParkirManual.java        # Input manual kendaraan
│   ├── RiwayatParkir.java       # Manajemen riwayat
│   ├── CekTarif.java           # Kalkulator tarif
│   ├── LoginPage.java          # Halaman login
│   └── DatabaseConnection.java  # Koneksi database
├── database/
│   └── smart_parking_schema.sql # Schema database
├── captured_images/            # Folder gambar hasil capture
├── tessdata/                   # Data training Tesseract
├── lib/                        # Library eksternal
├── pom.xml                     # Maven configuration
└── README.md
```

## 🚀 Instalasi dan Setup

### Prerequisites
- Java Development Kit (JDK) 21+
- MySQL Server 8.0+
- Maven 3.6+
- OpenCV 4.8.0 (native libraries)
- Tesseract OCR Engine
- Webcam/Camera untuk fitur scan

### 1. Clone Repository
```bash
git clone <repository-url>
cd Smart-Parkir
```

### 2. Setup Database
```sql
-- Buat database
CREATE DATABASE smart_parking;

-- Import schema
mysql -u root -p smart_parking < database/smart_parking_schema.sql
```

### 3. Konfigurasi Database
Edit file `DatabaseConnection.java` sesuai konfigurasi MySQL Anda:
```java
private static final String URL = "jdbc:mysql://localhost:3306/smart_parking";
private static final String USERNAME = "your_username";
private static final String PASSWORD = "your_password";
```

### 4. Install Dependencies
```bash
mvn clean install
```

### 5. Setup OpenCV
- Download OpenCV 4.8.0
- Extract ke `C:\opencv`
- Pastikan path `opencv_java480.dll` sesuai di `ScanPlatNomor.java`

### 6. Setup Tesseract
- Install Tesseract OCR
- Download language data (eng.traineddata) ke folder `tessdata/`

### 7. Compile dan Run
```bash
mvn compile exec:java -Dexec.mainClass="org.example.LoginPage"
```

## 🔐 Default Login

- **Username**: `admin`
- **Password**: `admin123`

atau

- **Username**: `operator`  
- **Password**: `operator123`

## 📱 Penggunaan

### 1. Login ke Sistem
Gunakan kredensial default atau yang sudah dikonfigurasi.

### 2. Dashboard Admin
- Monitoring kendaraan masuk/keluar hari ini
- Total kendaraan terparkir
- Pendapatan harian
- Akses ke semua fitur sistem

### 3. Scan Plat Nomor
- Klik "Start Camera" untuk mengaktifkan kamera
- Posisikan plat nomor di depan kamera
- Klik "Capture" untuk mengambil gambar
- Klik "Process OCR" untuk deteksi teks
- Pilih jenis kendaraan dan simpan

### 4. Parkir Manual
- Input plat nomor secara manual
- Pilih jenis kendaraan
- Tambahkan catatan (opsional)
- Simpan data

### 5. Verifikasi Plat
- Masukkan plat nomor kendaraan
- Sistem akan menampilkan info kendaraan
- Proses keluar kendaraan dan pembayaran

### 6. Riwayat Parkir
- Filter berdasarkan status, jenis kendaraan, atau plat nomor
- Edit/hapus data kendaraan
- Export ke CSV atau print laporan

## 🗄️ Database Schema

### Tabel `parking_entries`
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `plate_number` (VARCHAR(20), NOT NULL)
- `vehicle_type` (ENUM: 'MOTOR', 'MOBIL', 'TRUK')
- `entry_time` (TIMESTAMP)
- `exit_time` (TIMESTAMP, NULLABLE)
- `duration_minutes` (INT, NULLABLE)
- `fee` (DECIMAL(10,2), NULLABLE)
- `status` (ENUM: 'ACTIVE', 'COMPLETED')
- `notes` (TEXT, NULLABLE)

### Tabel `users`
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `username` (VARCHAR(50), UNIQUE)
- `password` (VARCHAR(255))
- `full_name` (VARCHAR(100))
- `role` (ENUM: 'admin', 'operator')

### Tabel `scan_sessions`
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `plate_number` (VARCHAR(20))
- `original_image_path` (VARCHAR(500))
- `processed_image_path` (VARCHAR(500))
- `ocr_raw_text` (TEXT)
- `scan_time` (TIMESTAMP)
- `notes` (TEXT)
- `success` (BOOLEAN)

## 🔧 Konfigurasi

### OCR Settings
- Engine: Tesseract 5.8.0
- Language: English (eng)
- Multiple PSM modes untuk akurasi optimal
- Image preprocessing untuk hasil OCR yang lebih baik

### Camera Settings
- Format: DirectShow (Windows)
- Resolution: 640x480
- Frame rate: 30 FPS
- Auto-recovery mechanism

## 🐛 Troubleshooting

### Camera Issues
- Pastikan kamera tidak digunakan aplikasi lain
- Check permission kamera di Windows
- Restart aplikasi jika kamera error

### OCR Issues
- Pastikan pencahayaan cukup
- Posisikan plat nomor tegak lurus kamera
- Clean gambar yang blur atau terdistorsi

### Database Connection
- Verify MySQL service berjalan
- Check username/password database
- Ensure database schema sudah diimport

## 📊 Performance Tips

- Gunakan SSD untuk performa database yang lebih baik
- Cleanup `captured_images/` folder secara berkala
- Monitor log files untuk troubleshooting
- Regular backup database

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Rafif** - *Initial work*

## 🙏 Acknowledgments

- OpenCV team for computer vision library
- Tesseract OCR team for text recognition engine
- FlatLaf team for modern Java Swing look and feel
- MySQL team for reliable database system

## 📞 Support

Jika Anda mengalami masalah atau memiliki pertanyaan:

1. Check troubleshooting section di atas
2. Review log files di `ocr_debug.log`
3. Check database connection dan schema
4. Ensure semua dependencies terinstall dengan benar

---

*Made with ❤️ for efficient parking management*