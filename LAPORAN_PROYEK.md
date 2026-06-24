# Laporan Akhir Proyek Mobile - LuxeThreads 👔✨
**Mata Kuliah: Pemrograman Mobile 2024**

---

## 1. Perkenalan Anggota Kelompok & Pembagian Peran
Laporan ini disusun oleh kelompok yang berfokus pada pengembangan aplikasi e-commerce premium:
*   **[Nama Anggota 1] - [NIM]**
    *   **Role**: Android Lead Developer. Bertanggung jawab atas struktur Activity, implementasi RecyclerView, integrasi Retrofit, dan logika bisnis aplikasi.
*   **[Nama Anggota 2] - [NIM]**
    *   **Role**: Backend & Database Engineer. Bertanggung jawab atas manajemen database MySQL, pembuatan API PHP, dan konfigurasi Firebase Console (SHA-1/SHA-256).
*   **[Nama Anggota 3] - [NIM]**
    *   **Role**: UI/UX Designer & Tester. Bertanggung jawab atas aset visual, implementasi Material Design 3, Dark Mode, dan pengujian QA (Quality Assurance) pada perangkat fisik.

---

## 2. Latar Belakang Proyek: LuxeThreads
Industri fashion saat ini menuntut kecepatan akses dan tampilan yang mewah. **LuxeThreads** hadir sebagai solusi aplikasi mobile yang tidak hanya berfungsi sebagai alat transaksi, tetapi juga memberikan pengalaman "Luxury Digital" kepada pengguna.

**Alasan Teknis Pemilihan Arsitektur Hybrid:**
Kami menggabungkan dua teknologi backend untuk efisiensi maksimal:
*   **Firebase**: Digunakan untuk **Authentication** (Google Sign-In) yang aman dan **Firestore** untuk sinkronisasi role secara real-time dan caching data pengguna.
*   **MySQL (XAMPP)**: Digunakan untuk mengelola data operasional yang berat seperti katalog barang, keranjang belanja, dan riwayat transaksi karena fleksibilitasnya dalam query relasional.

---

## 3. Penjelasan Metodologi SDLC (Waterfall)
Proyek ini dikembangkan menggunakan model **Waterfall** yang terstruktur:
1.  **Requirement Analysis**: Pengumpulan kebutuhan fitur seperti Multi-role (Admin & Pengunjung), sistem keranjang belanja, dan manajemen inventaris.
2.  **System Design**: Perancangan skema database MySQL (4 tabel) dan desain antarmuka menggunakan **Material Design 3** untuk mencapai kesan modern dan bersih.
3.  **Implementation (Coding)**:
    *   Bahasa: **Java**.
    *   Library Utama: **Retrofit2** (API), **Glide** (Image Loading), **Firebase SDK**, dan **Material Components**.
4.  **Integration & Testing**: Integrasi API PHP dengan Retrofit, pengujian konektivitas menggunakan IP laptop (Wi-Fi), dan debugging Logcat untuk menangani error network.
5.  **Deployment**: Konfigurasi server lokal XAMPP dan registrasi Fingerprint (SHA) aplikasi ke Firebase untuk akses Google Sign-In.

---

## 4. Demonstrasi Alur Aplikasi (User Experience)
Aplikasi membedakan pengalaman pengguna berdasarkan role:

*   **Alur Pengunjung**: 
    *   **Onboarding**: Splash Screen otomatis mengecek sesi login (FirebaseAuth).
    *   **Discovery**: Katalog produk dengan fitur pencarian responsif.
    *   **Transaction**: Menambah produk ke keranjang, mengelola jumlah (qty), dan melakukan checkout dengan alamat pengiriman.
*   **Alur Admin**: 
    *   **Analytics**: Dashboard statistik real-time yang menampilkan Total Pendapatan (Revenue) dengan format Rupiah.
    *   **Control Center**: Akses ke manajemen barang, daftar pelanggan (Member List), dan riwayat semua pesanan masuk.

---

## 5. Demonstrasi Fitur CRUD (Create, Read, Update, Delete)
Fitur CRUD diimplementasikan secara dinamis melalui API:
*   **Create**: Menambah produk melalui `TambahProdukActivity`. Menggunakan **Multipart Request** untuk mengupload file gambar asli dari galeri ke server.
*   **Read**: Data diambil dari database melalui endpoint `get_barang.php` dan ditampilkan menggunakan `RecyclerView` dengan `BajuAdminAdapter`.
*   **Update**: Mengubah detail produk (stok, harga, nama) melalui `EditProdukActivity` yang mengirimkan `id_barang` sebagai kunci utama.
*   **Delete**: Penghapusan data di MySQL menggunakan Dialog Konfirmasi untuk mencegah kesalahan hapus, diikuti dengan notifikasi real-time di UI.

---

## 6. Demonstrasi Sistem Login & Logout (Hybrid Security)
Keamanan aplikasi LuxeThreads menggunakan sistem dua lapis:
1.  **Google Sign-In (OAuth 2.0)**: Pengunjung dapat masuk tanpa formulir panjang. Token ID Google diverifikasi oleh Firebase Auth untuk menjamin keaslian akun.
2.  **Email & Password**: Admin masuk menggunakan kredensial yang tersimpan di Firebase Auth.
3.  **Role Redirection**: `SplashActivity` mengecek data pengguna di Firestore. Jika field `role` bernilai 'admin', aplikasi membuka `AdminActivity`. Jika 'pengunjung', membuka `PengunjungActivity`.
4.  **Secure Logout**: Menggunakan `mAuth.signOut()` dan membersihkan tumpukan aktivitas (*Intent.FLAG_ACTIVITY_CLEAR_TASK*) untuk mencegah akses balik setelah logout.

---

## 7. Penjelasan Struktur Database (ERD)
Sistem menggunakan database relasional `db_tokobaju` dengan struktur:
*   **Tabel `users`**: Menyimpan metadata user, UID Firebase, dan role.
*   **Tabel `barang`**: Menyimpan katalog produk (ID, Nama, Harga, Stok, Nama_File_Gambar).
*   **Tabel `keranjang`**: Tabel relasi antara User dan Barang untuk menyimpan pilihan belanja sementara.
*   **Tabel `transaksi`**: Mencatat semua penjualan sukses yang digunakan untuk menghitung statistik pendapatan di Dashboard Admin.

**Integrasi Data**: Aplikasi berkomunikasi dengan database menggunakan format **JSON** yang diproses oleh Retrofit2 melalui class `ApiClient`.

---

## 8. Kesimpulan & Saran
Proyek **LuxeThreads** berhasil mengintegrasikan teknologi Cloud (Firebase) dan Lokal (MySQL) dalam satu aplikasi mobile yang kohesif. Penggunaan metodologi Waterfall membantu tim tetap fokus pada fitur inti.

**Pencapaian Utama:**
*   Implementasi Google Sign-In yang stabil.
*   Sistem manajemen stok yang real-time bagi admin.
*   Antarmuka yang responsif dengan dukungan Dark Mode.

**Saran Pengembangan**: Kedepannya aplikasi dapat ditambahkan fitur Payment Gateway (seperti Midtrans) dan integrasi Maps API untuk pelacakan pengiriman barang secara real-time.

---
*Laporan ini disusun sebagai bukti pemenuhan kompetensi Pemrograman Mobile 2024.*
