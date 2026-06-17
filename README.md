# LuxeThreads - Premium Luxury E-commerce Mobile App 👔✨

Aplikasi toko baju kelas atas yang mengintegrasikan **Firebase Authentication** untuk keamanan gerbang masuk dan **MySQL/PHP** sebagai pusat data operasional. Dilengkapi dengan antarmuka mewah, animasi responsif, dan sistem multi-peran (Admin & Pengunjung).

---

## 🚀 Fitur Utama
*   **Professional Splash Screen**: Pembukaan aplikasi elegan dengan logo premium.
*   **Hybrid Authentication**: 
    *   **Admin**: Login menggunakan Email & Password via Firebase Auth Core.
    *   **Pengunjung**: Login instan menggunakan **Google Sign-In (SSO)**.
*   **Admin Dashboard Insight**: Statistik real-time (Total Produk, Pesanan, dan Omzet).
*   **Inventory Control**: Manajemen stok barang (CRUD) lengkap dengan upload nama file gambar.
*   **Admin Quota System**: Pendaftaran Admin dibatasi maksimal **3 orang** untuk keamanan.
*   **Full Shopping Flow**: Tambah keranjang, kelola item belanja, hingga proses Checkout berhasil.
*   **Premium UI/UX**: Palet warna Gold & Charcoal, teks besar yang jelas, dan animasi scale pada setiap tombol.

---

## 🛠️ 1. Persiapan Server (XAMPP & MySQL)

### A. Struktur Folder API
Pindahkan/buat folder API Anda di `C:\xampp\htdocs\api_tokobaju\`. Pastikan strukturnya seperti ini:
```
api_tokobaju/
├── images/             <-- Taruh file gambar produk (.jpg) di sini
├── koneksi.php
├── login.php
├── register.php
├── get_barang.php
├── tambah_barang.php
├── update_barang.php
├── hapus_barang.php
├── add_to_cart.php
├── get_cart.php
├── delete_cart.php
├── checkout.php
└── admin_service.php   <-- Untuk statistik & cek kuota admin
```

### B. Konfigurasi Database (SQL Final)
Buka `localhost/phpmyadmin`, buat database `db_tokobaju`, lalu jalankan Query ini:
```sql
-- 1. Tabel Users (Kunci Utama Hybrid Firebase-MySQL)
CREATE TABLE users (
    id_user INT(11) PRIMARY KEY AUTO_INCREMENT,
    uid_firebase VARCHAR(255) UNIQUE NOT NULL,
    nama VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'pengunjung') NOT NULL DEFAULT 'pengunjung',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Tabel Barang (Inventaris)
CREATE TABLE barang (
    id_barang INT(11) PRIMARY KEY AUTO_INCREMENT,
    nama_barang VARCHAR(255) NOT NULL,
    harga INT(11) NOT NULL,
    stok INT(11) NOT NULL,
    gambar VARCHAR(255) NOT NULL DEFAULT 'default.jpg'
) ENGINE=InnoDB;

-- 3. Tabel Keranjang (Shopping Cart)
CREATE TABLE keranjang (
    id_cart INT(11) PRIMARY KEY AUTO_INCREMENT,
    id_user VARCHAR(255) NOT NULL,
    id_barang INT(11) NOT NULL,
    jumlah INT(11) NOT NULL DEFAULT 1,
    FOREIGN KEY (id_barang) REFERENCES barang(id_barang) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Tabel Transaksi (Pesanan & Omzet)
CREATE TABLE transaksi (
    id_transaksi INT(11) PRIMARY KEY AUTO_INCREMENT,
    id_user VARCHAR(255) NOT NULL,
    total_harga INT(11) NOT NULL,
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Selesai', 'Berlangsung', 'Gagal') DEFAULT 'Selesai'
) ENGINE=InnoDB;

-- 5. Data Awal (Wajib agar Gambar Muncul)
INSERT INTO barang (nama_barang, harga, stok, gambar) VALUES 
('Premium Denim Jacket', 750000, 15, 'denim.jpg'),
('Silk Elegance Dress', 1200000, 8, 'dress.jpg'),
('Midnight Velvet Jacket', 850000, 12, 'jacket.jpg'),
('Classic Slim Pants', 450000, 20, 'pants.jpg'),
('Classic Oxford Shirt', 350000, 25, 'shirt.jpg');
```

---

## 🔥 2. Konfigurasi Firebase (Keamanan)

1.  Aktifkan **Authentication** (Email/Pass & Google) di Firebase Console.
2.  Aktifkan **Cloud Firestore** (Start in Test Mode).
3.  Daftarkan **SHA-1** laptop Anda ke Project Settings Firebase.
    *   *Cara ambil SHA-1*: Jalankan `./gradlew signingReport` di Terminal Android Studio.
4.  Download `google-services.json` dan letakkan di folder `app/` proyek.

---

## 📱 3. Konfigurasi Aplikasi (Android Studio)

1.  **Cek IP Wi-Fi Laptop**: Buka CMD, ketik `ipconfig`. (Berdasarkan testing terakhir: **`192.168.1.16`**).
2.  **Update Alamat Server**: Buka `com.bagus.toko_baju_uas.api.ApiClient.java`.
    ```java
    public static final String IP_LAPTOP = "192.168.1.16"; 
    ```
3.  **Sync & Build**: Klik **Sync Project with Gradle Files** lalu jalankan aplikasi.

---

## 🧪 4. Panduan Pengujian (Testing)

### Role Admin (Maks 3 Orang)
*   Daftar melalui menu **Sign Up**, pilih role **Admin**.
*   Jika sudah ada 3 admin di database, pendaftaran Admin ke-4 akan ditolak otomatis.
*   Dashboard akan menampilkan total pendapatan dan jumlah pesanan secara otomatis.

### Role Pengunjung
*   Gunakan tombol **Continue with Google** untuk akses instan.
*   Pencarian: Ketik "Denim" di search bar untuk filter instan.
*   Belanja: Klik (+) pada produk, lalu cek menu **Bag** untuk Checkout.

---

## 🆘 Troubleshooting
*   **Gambar Tidak Muncul**: Pastikan ekstensi file di folder `images` sama dengan di database (contoh: `shirt.jpg` vs `shirt.jpg`).
*   **Koneksi Timeout**: Matikan **Windows Firewall** atau pastikan HP dan Laptop di Wi-Fi yang sama.
*   **Google Login Failed**: Biasanya SHA-1 di Firebase Console belum diupdate atau belum Sync Gradle.

---
*LuxeThreads App - Ultimate Luxury Fashion Experience*
