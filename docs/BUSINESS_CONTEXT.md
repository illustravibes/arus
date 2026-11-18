# BUSINESS_CONTEXT — Arus MVP Rebuild

> **Status:** konteks produk untuk MVP rebuild, 7 November 2026. Baca bersama [PRD](PRD.md), [BUSINESS_RULES](BUSINESS_RULES.md), dan [USER_FLOWS](USER_FLOWS.md). Dokumen ini membedakan keputusan yang sudah disepakati dari asumsi yang perlu validasi.

## 1. Mengapa Arus ada

**Masalah:** keuangan pribadi mudah kehilangan jejak. Saldo uang tersebar di beberapa dompet, pencatatan sering tertunda, dan pengguna tidak selalu memahami mengapa saldonya menjadi suatu angka.

**Solusi MVP:** aplikasi Android **offline-first** yang memungkinkan pencatatan pemasukan, pengeluaran, dan transfer secara cepat; menghitung saldo dari catatan sumber; lalu menampilkan ringkasan dan grafik agar arus uang mudah dibaca.

**Janji produk:** “Catat uang, pantau saldo, pahami arus keuangan.”

**Prinsip:** *Less noise. More control.*

## 2. Pengguna dan pekerjaan utama

- **Pengguna sasaran:** individu yang mengurus keuangan pribadi dengan satu atau lebih dompet, misalnya uang tunai dan rekening/e-wallet.
- **Kebutuhan utama:** tahu saldo setiap dompet, bisa menemukan transaksi, dan memahami pemasukan/pengeluaran pada suatu periode.
- **Tidak ditargetkan pada MVP:** akuntansi usaha, pencatatan banyak pengguna, penggabungan akun keluarga, sinkronisasi antarperangkat, atau pencatatan bank otomatis.

Pekerjaan pengguna (*jobs to be done*):

1. **Saat menerima atau menggunakan uang**, saya ingin mencatatnya segera supaya saldo tidak mudah meleset.
2. **Saat memindahkan uang antar dompet**, saya ingin kedua saldo berubah benar tanpa terlihat seperti pendapatan atau pengeluaran baru.
3. **Saat mengecek kondisi keuangan**, saya ingin melihat saldo dan tren uang masuk/keluar tanpa menghitung manual.
4. **Saat mengganti perangkat**, saya ingin dapat membawa data saya melalui backup terenkripsi.

## 3. Nilai bisnis produk dan prioritas

| Urutan | Kebutuhan | Keberhasilan bagi pengguna |
|---|---|---|
| 1 | Catat uang masuk/keluar | Nominal tersimpan dan tercermin di saldo/riwayat. |
| 2 | Dompet dan transfer | Saldo per dompet serta total gabungan tetap dapat dijelaskan. |
| 3 | Laporan dan grafik | Ringkasan sama dengan sumber transaksi dan mudah dipahami. |
| 4 | Backup/restore | Data dapat diselamatkan tanpa akun/cloud. |

**Prioritas mutlak: kebenaran data lebih penting daripada banyaknya fitur atau animasi.** Tidak ada monetisasi, iklan, sosial, atau server pada cakupan MVP.

## 4. Batas produk (MVP)

**Termasuk:** pembuatan/pengelolaan dompet; kategori pemasukan/pengeluaran; transaksi; transfer; riwayat dan pencarian; Beranda; laporan/grafik periode harian, mingguan, bulanan; backup/restore lokal terenkripsi.

**Tidak termasuk:** OCR struk, scan, group order, utang-piutang, anggaran, split kategori, rekonsiliasi/checkpoint, transaksi rutin, pengingat, biaya admin transfer, akun/login, cloud sync, integrasi bank, multi-mata uang, dan ekspor laporan PDF/Excel.

> **Penting:** cakupan Arus lama lebih luas. Dokumentasi dan UI mockup lama tidak boleh otomatis mengaktifkan fitur di luar MVP. Jika screenshot menampilkan “Scan”, jangan jadikan aksi aktif pada MVP.

## 5. Perjalanan pengguna versi sederhana

```text
Buka aplikasi pertama kali
  → Buat dompet pertama (nama + saldo awal)
  → Beranda menampilkan saldo
  → Tombol (+): Pemasukan | Pengeluaran | Transfer
  → Lihat Riwayat / edit / hapus
  → Lihat Laporan + grafik
  → Backup melalui Pengaturan
```

Setiap langkah harus tetap bisa digunakan tanpa koneksi internet. Tidak ada proses sign-in.

## 6. Apa yang perlu diukur sebelum rilis

Tidak perlu SDK analytics eksternal. Gunakan **checklist pengujian dan uji pengguna manual**:

- Pengguna baru dapat membuat dompet dan mencatat transaksi pertama tanpa panduan khusus.
- Pengguna dapat menjelaskan mengapa saldo dompet sesuai dengan riwayatnya.
- Pengguna bisa menemukan dan memperbaiki transaksi salah.
- Grafik dan angka ringkasan selalu cocok dengan hasil agregasi transaksi.
- Backup dapat dipulihkan pada instalasi baru menggunakan kata sandi yang benar.
- Alur inti bekerja saat perangkat offline.

Belum ada target numerik resmi seperti conversion rate atau durasi input. Jangan mengarang KPI atau menanam telemetry tanpa keputusan pemilik proyek.

## 7. Keputusan yang sudah tetap vs yang belum diputuskan

**Tetap:** Android native Kotlin; offline-first; IDR saja; schema Room baru v1 (belum ada pengguna/data produksi); 4 tab + tombol (+); grafik batang; penyimpanan lokal terenkripsi; tidak ada migrasi database legacy.

**Keputusan pemilik proyek (7 November 2026):** saldo dompet boleh negatif; pengeluaran/transfer tidak ditolak hanya karena saldo tidak cukup; saldo awal boleh diedit setelah transaksi tercatat. Mengedit saldo awal mengubah saldo terhitung tanpa mengubah transaksi historis.

**Keputusan pemilik proyek:** dompet tidak diarsipkan pada MVP. Dompet yang memiliki transaksi tetap tersedia agar histori dan saldo tetap terlihat.

## 8. Istilah bisnis

- **Dompet:** tempat uang dicatat, misalnya Tunai, Rekening, atau E-Wallet (bukan koneksi bank).
- **Saldo awal:** uang yang sudah ada saat dompet dibuat; bukan pemasukan dalam laporan.
- **Pemasukan:** transaksi yang menambah uang pada dompet dan dihitung dalam laporan.
- **Pengeluaran:** transaksi yang mengurangi uang pada dompet dan dihitung dalam laporan.
- **Transfer:** pemindahan uang antara dua dompet milik pengguna; tidak dihitung sebagai pemasukan/pengeluaran.
- **Transaksi efektif:** tanggal transaksi sudah tiba menurut zona waktu lokal yang digunakan aplikasi.
- **Saldo saat ini:** saldo awal ditambah efek seluruh transaksi efektif.
- **Selisih laporan:** total pemasukan dikurangi total pengeluaran pada periode/filter terpilih; bukan otomatis sama dengan total saldo dompet.

## 9. Urutan membaca bagi AI Agent

1. [PRD.md](PRD.md) — **apa** yang masuk scope.
2. Dokumen ini — **mengapa** produk ada dan untuk siapa.
3. [BUSINESS_RULES.md](BUSINESS_RULES.md) — **aturan pasti** saldo, transaksi, laporan, dan integritas.
4. [USER_FLOWS.md](USER_FLOWS.md) — **bagaimana** pengguna menyelesaikan tugas.
5. [TECH_STACK_AND_UI.md](TECH_STACK_AND_UI.md) dan [DESIGN.md](../DESIGN.md) — **bagaimana mengimplementasikan dan menampilkan**.
