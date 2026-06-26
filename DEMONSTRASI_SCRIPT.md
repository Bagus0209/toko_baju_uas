# Skenario Demonstrasi Aplikasi - LuxeThreads 👔✨

Naskah ini dibagi menjadi 3 bagian utama untuk menunjukkan alur kerja aplikasi secara sistematis.

---

## 🔐 Bagian 1: Demonstrasi Login & Logout (Hybrid Auth)

**Tujuan**: Menunjukkan sistem keamanan pintu masuk aplikasi menggunakan Firebase & Google SSO.

| Langkah | Tindakan (Action) | Penjelasan (Narasi) |
| :--- | :--- | :--- |
| **1** | Buka aplikasi LuxeThreads. | "Selamat datang di LuxeThreads. Saat aplikasi pertama kali dibuka, Splash Screen akan mengecek validitas sesi pengguna secara real-time." |
| **2** | Klik tombol **'Continue with Google'**. | "Untuk pengunjung, kami menyediakan fitur Google Sign-In (SSO) agar akses masuk lebih instan tanpa perlu mengisi formulir panjang." |
| **3** | Pilih akun Google Anda. | "Data unik dari Google (UID) akan disinkronkan ke database MySQL kami untuk mencatat profil pembeli." |
| **4** | Klik menu Profil -> **Logout**. | "Untuk keamanan, tombol Logout akan menghapus seluruh cache sesi di memori dan mengarahkan kembali ke gerbang login utama." |

---

## 📊 Bagian 2: Demonstrasi Aplikasi (User Experience)

**Tujuan**: Menunjukkan fitur unggulan interaktif (Onboarding & Product Tour).

| Langkah | Tindakan (Action) | Penjelasan (Narasi) |
| :--- | :--- | :--- |
| **1** | Login dengan akun pengunjung baru. | "Setelah login sebagai pengguna baru, sistem secara cerdas akan memicu fitur **Interactive Onboarding**." |
| **2** | Geser slide onboarding (1 s/d 5). | "Panduan ini tampil satu kali sehari selama tiga hari untuk membantu pengguna memahami nilai jual aplikasi kami." |
| **3** | Klik **'Mulai Belanja'**. | "Seketika setelah onboarding selesai, fitur **Intelligent Product Tour** aktif untuk menyoroti menu navigasi utama menggunakan tooltip interaktif." |
| **4** | Ketik 'Jacket' di Search Bar. | "Fitur pencarian kami bersifat responsif dan didukung oleh filter kategori seperti Jackets, Shirts, dan lainnya." |
| **5** | Klik ikon (+) pada produk -> Buka Keranjang. | "Proses belanja sangat lancar, pengguna bisa menambah jumlah item di keranjang sebelum melakukan checkout." |

---

## 🛠️ Bagian 3: Demonstrasi CRUD (Admin Control)

**Tujuan**: Menunjukkan kontrol penuh Admin atas inventaris barang dan data bisnis.

| Langkah | Tindakan (Action) | Penjelasan (Narasi) |
| :--- | :--- | :--- |
| **1** | Login menggunakan akun **Admin**. | "Sebagai Admin, saya diarahkan ke **Business Intelligence Dashboard** yang menampilkan statistik omzet dan pesanan secara real-time." |
| **2** | Klik **'Manage Inventory'**. | "Inilah fitur CRUD utama kami. Admin memiliki kontrol penuh atas stok barang yang tersedia di katalog." |
| **3** | **(Create)**: Klik tombol (+) -> Isi Nama, Harga, Stok -> Pilih Gambar dari Galeri. | "Saya akan menambah produk baru. Aplikasi mengupload file gambar asli ke server XAMPP melalui Multipart Request menggunakan Retrofit." |
| **4** | **(Read)**: Lihat produk baru muncul di daftar. | "Produk yang baru saja diinput langsung tersinkronisasi dan tampil di aplikasi dalam waktu kurang dari satu detik." |
| **5** | **(Update)**: Klik tombol Edit -> Ubah Stok menjadi 100. | "Jika ada perubahan inventaris, Admin cukup menekan tombol edit dan data di server MySQL akan diperbarui secara otomatis." |
| **6** | **(Delete)**: Klik tombol Hapus -> Konfirmasi. | "Kami menyertakan dialog konfirmasi sebelum menghapus data untuk mencegah terjadinya human error dalam pengelolaan barang." |

---

## 📝 Kesimpulan Penutup:
"Dengan kombinasi Firebase dan MySQL, LuxeThreads berhasil menciptakan ekosistem belanja yang aman bagi pengguna dan efisien bagi pengelola. Terima kasih."
