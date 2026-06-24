# LuxeThreads - Premium Luxury E-commerce Mobile App 👔✨
**Ultimate Fashion Experience | Proyek UAS Pemrograman Mobile 2024**

LuxeThreads adalah aplikasi e-commerce fashion kelas atas berbasis Android yang menggabungkan estetika desain mewah dengan fungsionalitas modern. Aplikasi ini menggunakan **Arsitektur Hybrid Backend** yang mengintegrasikan kecepatan **Firebase** untuk keamanan dan **MySQL/PHP** untuk manajemen data operasional yang kompleks.

---

## 🚀 Fitur Unggulan

### 🛡️ Keamanan & Autentikasi (Hybrid)
*   **Google Sign-In (SSO)**: Akses instan bagi pengunjung menggunakan akun Google.
*   **Email & Password Auth**: Sistem login tradisional yang aman dikelola oleh Firebase.
*   **Role-Based Access Control (RBAC)**: Pengalihan otomatis antara Dashboard Admin dan Katalog Pengunjung berdasarkan data Firestore.

### 🛍️ Pengalaman Belanja (Customer Side)
*   **Interactive Onboarding**: Panduan 5 slide yang elegan untuk memperkenalkan aplikasi (Tampil 1x sehari selama 3 hari).
*   **Intelligent Product Tour**: Tooltip interaktif (TapTargetView) yang menyoroti fitur navigasi utama saat pertama kali belanja.
*   **Advanced Catalog**: Filter kategori (Jackets, Shirts, Pants, Dresses) dan fitur pencarian responsif.
*   **Seamless Cart & Checkout**: Manajemen item keranjang (tambah/kurang jumlah) dan proses checkout dengan alamat pengiriman.
*   **Transaction History**: Pelacakan status pesanan secara real-time dengan fitur **Cetak Nota Digital**.

### 📊 Pusat Kendali (Admin Side)
*   **Business Intelligence Dashboard**: Statistik real-time mencakup Total Pendapatan, Total Pesanan, Produk Terdaftar, dan Jumlah Member.
*   **Inventory Management (CRUD)**: Kontrol stok penuh dengan fitur upload gambar langsung dari galeri HP ke server.
*   **Member Directory**: Daftar lengkap pelanggan terdaftar untuk mempermudah manajemen customer.

---

## 🛠️ Arsitektur Teknologi

| Komponen | Teknologi |
| :--- | :--- |
| **Bahasa Pemrograman** | Java (Android SDK) |
| **UI Framework** | Material Design 3 (Google) |
| **Networking** | Retrofit 2 & OkHttp3 Logging Interceptor |
| **Image Engine** | Glide (Image Caching & Processing) |
| **Cloud Services** | Firebase Auth & Cloud Firestore |
| **Backend** | PHP 7.4+ (REST API) |
| **Database** | MySQL (Relational Database) |

---

## 📋 Panduan Instalasi Detail

### 1. Konfigurasi Server Lokal (XAMPP)
1.  Pindahkan folder API ke: `C:\xampp\htdocs\api_tokobaju\`
2.  Pastikan folder **`images`** sudah ada di dalam direktori tersebut untuk menampung aset gambar.
3.  Buka `phpmyadmin`, buat database `db_tokobaju`, dan jalankan perintah SQL berikut:

```sql
-- Tabel Pengguna
CREATE TABLE users (
    id_user INT PRIMARY KEY AUTO_INCREMENT,
    uid_firebase VARCHAR(255) UNIQUE NOT NULL,
    nama VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    role ENUM('admin', 'pengunjung') DEFAULT 'pengunjung',
    onboarding_seen_count INT DEFAULT 0,
    onboarding_last_seen DATE NULL,
    onboarding_completed TINYINT(1) DEFAULT 0,
    onboarding_skipped TINYINT(1) DEFAULT 0
);

-- Tabel Inventaris
CREATE TABLE barang (
    id_barang INT PRIMARY KEY AUTO_INCREMENT,
    nama_barang VARCHAR(255) NOT NULL,
    harga INT NOT NULL,
    stok INT NOT NULL,
    gambar VARCHAR(255) DEFAULT 'default.jpg'
);

-- Tabel Transaksi & Keranjang (Lihat file .sql lengkap di folder database)
```

### 2. Konfigurasi Firebase (PENTING)
1.  Daftarkan aplikasi di [Firebase Console](https://console.firebase.google.com/).
2.  **Fingerprint SHA-1**: Ambil dari Terminal Android Studio dengan perintah `./gradlew signingReport`. Masukkan SHA-1 dan SHA-256 ke Project Settings.
3.  Download `google-services.json` dan letakkan di folder `app/`.
4.  Aktifkan **Authentication** (Google & Email) dan **Firestore Database**.
5.  Set Rules Firestore: `allow read, write: if request.auth != null;`

### 3. Konfigurasi Jaringan Android Studio
Aplikasi membutuhkan koneksi ke IP laptop Anda agar bisa mengakses database.
1.  Buka CMD, ketik `ipconfig`, cari **IPv4 Address** (Contoh: `192.168.1.12`).
2.  Buka `com.bagus.toko_baju_uas.api.ApiClient.java`.
3.  Update variabel: `public static String IP_LAPTOP = "192.168.1.12";`
4.  **Satu Jaringan**: Pastikan HP dan Laptop terhubung ke Wi-Fi yang sama.

---

## 🆘 Troubleshooting & FAQ

*   **Q: Kenapa Google Login Error 10?**
    *   A: SHA-1 di Firebase Console tidak cocok dengan SHA-1 laptop Anda. Update SHA-1 dan download ulang `google-services.json`.
*   **Q: Gambar produk tidak muncul?**
    *   A: Pastikan folder di htdocs bernama `images` (huruf kecil) dan IP di `ApiClient.java` sudah sesuai. Matikan Windows Firewall jika perlu.
*   **Q: Upload Gambar Gagal (Server Error 500)?**
    *   A: Cek `php.ini` di XAMPP, pastikan `upload_max_filesize` diatur minimal `10M`.

---

## 👨‍💻 Kontribusi Kelompok
*   **Project Lead**: [Nama Anda]
*   **UI/UX**: [Nama Anggota]
*   **Backend**: [Nama Anggota]

---
*LuxeThreads - Elegance in every tap. Disusun untuk memenuhi UAS Pemrograman Mobile 2024.*
