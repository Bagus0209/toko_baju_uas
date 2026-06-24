# LuxeThreads - Premium Luxury E-commerce Mobile App 👔✨

Aplikasi toko baju kelas atas yang mengintegrasikan **Firebase Authentication** untuk keamanan gerbang masuk dan **MySQL/PHP** sebagai pusat data operasional. Dilengkapi dengan antarmuka mewah, animasi responsif, dan sistem multi-peran (Admin & Pengunjung).

---

## 🛠️ Panduan Instalasi (Step-by-Step)

Jika Anda melakukan clone repositori ini, ikuti langkah-langkah detail berikut untuk menjalankan aplikasi:

### 1. Persiapan Server Lokal (XAMPP)
Aplikasi ini membutuhkan backend PHP dan database MySQL.

*   **Pindahkan Folder API:** Pindahkan folder API (backend) Anda ke dalam direktori `C:\xampp\htdocs\api_tokobaju\`.
*   **Struktur Folder:** Pastikan terdapat folder bernama `images` di dalam folder tersebut untuk menyimpan gambar produk.
    ```
    C:\xampp\htdocs\api_tokobaju\images\
    ```
*   **Import Database:**
    1. Buka browser dan akses `http://localhost/phpmyadmin`.
    2. Buat database baru dengan nama `db_tokobaju`.
    3. Import file SQL atau jalankan query berikut:

```sql
-- Tabel Utama
CREATE TABLE users (
    id_user INT PRIMARY KEY AUTO_INCREMENT,
    uid_firebase VARCHAR(255) UNIQUE NOT NULL,
    nama VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    role ENUM('admin', 'pengunjung') DEFAULT 'pengunjung'
);

CREATE TABLE barang (
    id_barang INT PRIMARY KEY AUTO_INCREMENT,
    nama_barang VARCHAR(255) NOT NULL,
    harga INT NOT NULL,
    stok INT NOT NULL,
    gambar VARCHAR(255) DEFAULT 'default.jpg'
);

CREATE TABLE keranjang (
    id_cart INT PRIMARY KEY AUTO_INCREMENT,
    id_user VARCHAR(255),
    id_barang INT,
    jumlah INT DEFAULT 1,
    FOREIGN KEY (id_barang) REFERENCES barang(id_barang) ON DELETE CASCADE
);

CREATE TABLE transaksi (
    id_transaksi INT PRIMARY KEY AUTO_INCREMENT,
    id_user VARCHAR(255),
    total_harga INT,
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Selesai', 'Berlangsung', 'Gagal') DEFAULT 'Selesai'
);
```

### 2. Konfigurasi Firebase (Penting)
Aplikasi ini tidak akan bisa login tanpa konfigurasi Firebase yang benar.

1.  **Buat Proyek:** Buka [Firebase Console](https://console.firebase.google.com/), buat proyek baru.
2.  **Tambah Aplikasi Android:** Masukkan package name `com.bagus.toko_baju_uas`.
3.  **SHA-1 Fingerprint:** Anda **WAJIB** mendaftarkan SHA-1 laptop Anda agar Google Sign-In berfungsi.
    *   Di Android Studio, buka tab **Terminal**.
    *   Jalankan: `./gradlew signingReport` (atau `gradlew signingReport` di Windows).
    *   Salin kode `SHA-1` dan `SHA-256` dari variant `debug`, lalu masukkan ke **Project Settings** di Firebase Console.
4.  **Download Config:** Download file `google-services.json` dan letakkan di folder `app/` proyek Anda.
5.  **Aktifkan Auth:** Di Firebase Console, aktifkan metode login **Email/Password** dan **Google**.
6.  **Firestore:** Aktifkan Firestore Database dan atur **Rules** agar dapat diakses:
    ```
    allow read, write: if request.auth != null;
    ```

### 3. Konfigurasi Koneksi Android ke Laptop
Agar HP/Emulator bisa mengakses database di laptop:

1.  **Cek IP Laptop:** Buka CMD, ketik `ipconfig`. Cari **IPv4 Address** (Contoh: `192.168.1.12`).
2.  **Update ApiClient:** Buka file `app/src/main/java/com/bagus/toko_baju_uas/api/ApiClient.java`.
3.  **Ubah Variabel IP:**
    ```java
    public static String IP_LAPTOP = "192.168.1.12"; // Ganti dengan IP laptop Anda
    ```
4.  **Satu Jaringan:** Pastikan HP dan Laptop terhubung ke **Wi-Fi yang sama**.
5.  **Firewall:** Jika koneksi gagal, matikan sementara **Windows Defender Firewall**.

---

## 🚀 Fitur Utama
*   **Hybrid Authentication**: Firebase Auth (Keamanan) + MySQL (Data Operasional).
*   **Multi-Role**: Admin (Dashboard Statistik & Inventaris) & Pengunjung (Belanja & History).
*   **Premium UI**: Desain mewah dengan palet warna Gold, Charcoal, dan sistem Dark Mode.
*   **Real-time Stats**: Grafik pendapatan dan jumlah pesanan otomatis bagi Admin.
*   **Smooth UX**: Animasi responsif pada setiap interaksi tombol.

---

## 🆘 Troubleshooting
*   **Koneksi Gagal:** Cek kembali IP Laptop di `ApiClient.java` dan pastikan Apache XAMPP sedang Running.
*   **Gambar Tidak Muncul:** Pastikan folder di htdocs bernama `images` dan izin akses folder sudah benar.
*   **Google Login Error 10:** Ini tanda SHA-1 di Firebase tidak cocok dengan laptop Anda. Update SHA-1 di Firebase Console.

---
*LuxeThreads - Ultimate Fashion Experience | UAS Pemrograman Mobile 2024*
