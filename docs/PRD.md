# PRD — Arus MVP (Rebuild)

> Status: rancangan MVP · Bahasa produk: Indonesia · Platform: Android native · Versi awal: `0.1.0`
> Lihat juga [konteks bisnis](BUSINESS_CONTEXT.md), [aturan bisnis](BUSINESS_RULES.md), dan [alur pengguna](USER_FLOWS.md).

## 1. Ringkasan

Arus adalah aplikasi pencatatan keuangan pribadi **offline-first** untuk mencatat arus uang, melihat saldo per dompet, dan memahami pemasukan/pengeluaran melalui laporan serta grafik sederhana.

**Tujuan inti:** pengguna dapat mencatat uang masuk/keluar dengan cepat dan mempercayai saldo yang ditampilkan.

**Target pengguna:** individu yang mengelola keuangan sendiri tanpa memerlukan koneksi internet, sinkronisasi bank, atau spreadsheet.

## 2. Cakupan MVP

| Fitur | Perilaku minimum |
|---|---|
| Dompet | Buat dan ubah nama dompet; isi saldo awal; tampilkan saldo terkini. Dompet tidak diarsipkan pada MVP. |
| Kategori | Buat, ubah, dan kelola kategori pemasukan dan pengeluaran. |
| Transaksi | Tambah pemasukan/pengeluaran dengan nominal, tanggal, dompet, kategori, dan catatan opsional. |
| Transfer | Pindahkan uang antar dua dompet berbeda; tercermin pada kedua saldo. |
| Riwayat | Daftar transaksi terbaru, detail, pencarian, filter dasar, edit, dan hapus dengan konfirmasi. |
| Beranda | Total saldo, ringkasan dompet, dan aktivitas terbaru; aksi tambah yang menonjol. |
| Laporan | Total pemasukan, pengeluaran, dan selisih per periode. |
| Grafik | Grafik batang perbandingan pemasukan vs pengeluaran; periode harian, mingguan, bulanan; filter semua/1 dompet. |
| Backup & restore | Ekspor backup terenkripsi melalui pemilih berkas Android dan pulihkan dengan konfirmasi. |

## 3. Alur utama

1. Pengguna membuka aplikasi dan membuat dompet pertama (nama + saldo awal).
2. Pengguna mencatat pemasukan/pengeluaran melalui tombol **(+)**.
3. Pengguna memeriksa perubahan saldo di Beranda dan riwayat Transaksi.
4. Pengguna membuka Laporan untuk membandingkan pemasukan-pengeluaran pada grafik.
5. Pengguna dapat membuat backup dan memulihkan data dari Pengaturan.

## 4. Aturan bisnis wajib

1. Mata uang MVP hanya **IDR**. Simpan nilai sebagai `Long` dalam satuan rupiah; **jangan gunakan `Float`/`Double` untuk uang**.
2. Nominal transaksi dan transfer **harus positif**; tipe transaksi menentukan arah mutasi.
3. Saldo dompet **diturunkan dari saldo awal + transaksi efektif**. Jangan menyimpan cache saldo sebagai sumber kebenaran kedua.
4. Transaksi bertanggal masa depan boleh dicatat, tetapi **belum mengubah saldo saat ini atau grafik realisasi** sampai tanggalnya efektif.
5. Transfer harus memindahkan nominal yang sama dari dompet asal ke dompet tujuan secara **atomik**. Tidak boleh transfer ke dompet yang sama.
6. Transfer **bukan pemasukan/pengeluaran** pada laporan dan grafik. Saldo total semua dompet tidak berubah karena transfer biasa.
7. Edit/hapus transaksi memperbarui perhitungan saldo, riwayat, dan laporan; tidak meninggalkan mutasi transfer sebelah saja.
8. Kategori harus memiliki jenis pemasukan/pengeluaran. Transfer tidak membutuhkan kategori.
9. Laporan dan grafik bersumber dari transaksi yang sama dengan perhitungan saldo; filter tanggal memakai zona waktu lokal perangkat dan batas periode yang konsisten.
10. Penghapusan transaksi dan restore memerlukan **konfirmasi eksplisit**; proses gagal tidak boleh merusak data lama.
11. Semua fitur inti bekerja **tanpa internet**.

> Keputusan MVP: belum ada pengguna dan data produksi. Database rebuild dapat dimulai pada **schema v1**; tidak diperlukan migrasi dari schema 21 Arus lama. Setelah data riil digunakan, setiap perubahan skema wajib menggunakan migrasi aman.

## 5. Navigasi

- **Beranda** — total saldo, dompet, aktivitas terbaru.
- **Transaksi** — daftar, cari, filter, detail, edit/hapus.
- **Laporan** — ringkasan dan grafik pemasukan/pengeluaran.
- **Pengaturan** — kelola dompet, kategori, backup/restore, preferensi.
- Tombol **(+)** — tambah pemasukan, pengeluaran, atau transfer.

Aturan UX: navigasi bawah pada halaman utama; subhalaman memakai tombol kembali yang jelas dan tidak menampilkan bottom navigation tanpa alasan.

## 6. Grafik keuangan

- **Jenis:** grafik batang perbandingan dua seri: pemasukan dan pengeluaran.
- **Periode:** harian, mingguan, bulanan; default bulanan.
- **Filter:** semua dompet atau satu dompet.
- **Ringkasan:** total pemasukan, total pengeluaran, dan selisih (`pemasukan - pengeluaran`).
- **Sumber:** hanya pemasukan/pengeluaran efektif; transaksi transfer dikecualikan.
- **Interaksi minimum:** label sumbu/angka mudah dibaca, empty state saat belum ada data, aksesibilitas menyampaikan angka dalam teks, bukan warna saja.
- Untuk periode tanpa transaksi, tampilkan `Rp0`; gunakan pembagian tanggal yang konsisten (mis. minggu Senin–Minggu).

## 7. Di luar MVP

OCR struk, group order, utang-piutang, rekonsiliasi lanjutan/checkpoint, split transaksi, anggaran, transaksi rutin/pengingat, biaya admin transfer, sinkronisasi cloud, akun/login, integrasi bank, multi-currency, analytics/telemetry jarak jauh, ekspor laporan PDF/Excel.

**Jangan menambahkan fitur ini tanpa pembaruan PRD dan persetujuan pemilik proyek.**

## 8. Kriteria penerimaan

- Transaksi pemasukan/pengeluaran membuat saldo berubah tepat sesuai nominal.
- Transfer tidak mengubah total saldo semua dompet dan tidak muncul sebagai pemasukan/pengeluaran di laporan.
- Edit/hapus transaksi langsung memperbarui saldo dan grafik.
- Transaksi masa depan tidak masuk perhitungan realisasi hari ini.
- Grafik cocok dengan agregasi transaksi pada periode/filter yang sama, termasuk saat kosong.
- Aplikasi tetap dapat digunakan dalam airplane mode.
- Backup dapat dipulihkan pada instalasi/perangkat lain dengan kata sandi yang benar; kata sandi salah atau berkas rusak tidak merusak data yang sudah ada.
- Alur inti nyaman di layar kecil dan didukung pembaca layar serta pembesaran font.

## 9. Definisi selesai (MVP)

Fitur dianggap selesai jika implementasi, pengujian aturan bisnis, empty/error states, dan verifikasi UI pada emulator/perangkat telah dilakukan. **Build berhasil saja bukan bukti siap rilis.**
