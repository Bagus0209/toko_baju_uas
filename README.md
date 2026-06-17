# LuxeThreads - Luxury E-commerce Mobile App 👔✨

Aplikasi toko baju premium yang mengintegrasikan **Firebase Authentication** untuk keamanan tingkat tinggi dan **MySQL/PHP** sebagai pusat data inventaris.

---

## 🚀 Fitur Utama
*   **Multi-Role Authentication**: Pemisahan hak akses antara **Admin** dan **Pengunjung**.
*   **Google Sign-In (SSO)**: Login instan untuk pelanggan menggunakan akun Google.
*   **Hybrid Database**: Menggunakan Firebase Firestore (Role Management) dan MySQL (Product Management).
*   **Dynamic Inventory**: Fitur CRUD (Create, Read, Update, Delete) produk lengkap untuk Admin.
*   **Premium UI/UX**: Desain mewah dengan palet warna Gold & Charcoal serta animasi tombol interaktif.

---

## 🛠️ Persiapan Server (MySQL & XAMPP)

1.  **Struktur Folder**:
    Pindahkan folder API Anda ke dalam `C:\xampp\htdocs\api_tokobaju\`. Pastikan struktur folder sebagai berikut:
    ```
    api_tokobaju/
    ├── images/          <-- Letakkan file .jpg di sini (denim.jpg, shirt.jpg, dll)
    ├── koneksi.php
    ├── login.php
    ├── register.php
    ├── get_barang.php
    ├── tambah_barang.php
    ├── update_barang.php
    └── hapus_barang.php
    ```

2.  **Konfigurasi Database**:
    Buka `phpMyAdmin`, buat database baru bernama `db_tokobaju`, dan jalankan query berikut:
    ```sql
    CREATE TABLE barang (
        id_barang INT(11) PRIMARY KEY AUTO_INCREMENT,
        nama_barang VARCHAR(255) NOT NULL,
        harga INT(11) NOT NULL,
        stok INT(11) NOT NULL,
        gambar VARCHAR(255) NOT NULL
    ) ENGINE=InnoDB;

    INSERT INTO barang (nama_barang, harga, stok, gambar) VALUES 
    ('Premium Denim Jacket', 750000, 15, 'denim.jpg'),
    ('Silk Elegance Dress', 1200000, 8, 'dress.jpg'),
    ('Midnight Velvet Jacket', 850000, 12, 'jacket.jpg'),
    ('Classic Slim Pants', 450000, 20, 'pants.jpg'),
    ('Classic Oxford Shirt', 350000, 25, 'shirt.jpg');

    CREATE TABLE users (
        id_user INT(11) PRIMARY KEY AUTO_INCREMENT,
        nama VARCHAR(100) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        role ENUM('admin', 'pengunjung') NOT NULL DEFAULT 'pengunjung'
    ) ENGINE=InnoDB;
    ```

---

## 🔥 Konfigurasi Firebase

1.  Daftarkan aplikasi ke [Firebase Console](https://console.firebase.google.com/).
2.  Aktifkan **Authentication** (Metode Email/Pass dan Google).
3.  Aktifkan **Cloud Firestore** (Standard Edition).
4.  Masukkan **SHA-1** laptop Anda ke Project Settings Firebase.
5.  Download `google-services.json` dan letakkan di folder `app/` proyek ini.

---

## 📱 Menjalankan Aplikasi

1.  **Cek IP Laptop**: Buka CMD, ketik `ipconfig`, cari IPv4 Address (contoh: `192.168.1.16`).
2.  **Update ApiClient**: Buka `app/src/main/java/com/bagus/toko_baju_uas/api/ApiClient.java`.
    ```java
    public static final String IP_LAPTOP = "192.168.1.16"; // Ganti dengan IP Anda
    ```
3.  **Sync Project**: Klik tombol **Sync Project with Gradle Files** (ikon gajah).
4.  **Run**: Jalankan di HP Fisik atau Emulator (Pastikan satu jaringan Wi-Fi).

---

## 🧪 Panduan Testing
*   **Role Admin**: Daftar akun baru via menu Sign Up, pilih role Admin. Login, lalu coba tambah atau edit produk.
*   **Role Pengunjung**: Login menggunakan tombol **Continue with Google**. Coba fitur filter kategori dan search baju.
*   **Animasi**: Tekan tombol apa saja untuk melihat efek animasi "Scale Click".

---

## 🆘 Troubleshooting
*   **Koneksi Gagal**: Pastikan IP di `ApiClient.java` sudah benar dan matikan **Windows Firewall**.
*   **Gambar Tidak Muncul**: Pastikan nama file di folder `images` sama persis dengan nama file di database (contoh: `shirt.jpg` harus sama dengan `shirt.jpg`).
*   **Google Login Error**: Pastikan SHA-1 sudah terdaftar di Firebase Console dan sudah update file `google-services.json`.

---
*Dibuat untuk keperluan UAS - LuxeThreads App 2024*
