# USER_FLOWS — Alur Pengguna Arus MVP

> Alur bisnis dan penerimaan fitur, **bukan desain final screen pixel-by-pixel**. UI mengikuti [DESIGN.md](../DESIGN.md). Aturan keuangan mengikuti [BUSINESS_RULES.md](BUSINESS_RULES.md).

## Peta navigasi

```text
                 [Beranda]
                /   |    \
        Dompet     (+)    Transaksi Terbaru
          |         |            |
      Detail      Pilih aksi   Detail transaksi
      Dompet      /   |   \       |  \
               Masuk Keluar Transfer  Edit Hapus

[Transaksi] → Cari / Filter → Detail → Edit / Hapus
[Laporan]  → Periode + Dompet → Ringkasan + Grafik
[Pengaturan] → Dompet / Kategori / Backup / Restore
```

Tab utama: **Beranda, Transaksi, Laporan, Pengaturan**. Tombol **(+)** ada untuk membuat pemasukan, pengeluaran, dan transfer. Subhalaman menggunakan tombol kembali.

## F-01 — Pengguna baru membuat dompet

**Pemicu:** pertama membuka Arus, database masih kosong.

1. Pengguna melihat penjelasan singkat bahwa data disimpan di perangkat, tanpa login.
2. Membuat dompet: isi **nama** dan **saldo awal** dalam IDR.
3. Validasi nama tidak kosong; saldo awal minimal Rp0.
4. Simpan dompet dan buka Beranda.

**Sukses:** Beranda menampilkan nama dompet dan saldo awal; belum ada pemasukan/pengeluaran di laporan.

**State lain:** form kosong/invalid tidak disimpan; simpan gagal menampilkan pesan dan dapat dicoba ulang.

## F-02 — Catat pemasukan

**Pemicu:** tombol (+) → Pemasukan.

1. Isi nominal positif, pilih dompet, pilih kategori pemasukan, tentukan tanggal; catatan opsional.
2. Tinjau ringkas informasi, lalu Simpan.
3. Aplikasi menyimpan transaksi dan kembali ke konteks yang masuk akal.

**Sukses:** transaksi muncul dalam riwayat; jika efektif, saldo dompet bertambah dan pemasukan periode/grafik ikut berubah.

**State lain:** nominal/kategori/dompet tidak valid ditolak; tanggal masa depan tidak memengaruhi saldo saat ini.

## F-03 — Catat pengeluaran

**Pemicu:** tombol (+) → Pengeluaran.

Alur sama seperti F-02, tetapi kategori harus bertipe **pengeluaran**. **Sukses:** saldo dompet berkurang dan pengeluaran periode/grafik bertambah pada tanggal efektif. Kebijakan ketika saldo tidak cukup masih harus ditetapkan (lihat [BUSINESS_RULES.md](BUSINESS_RULES.md)).

## F-04 — Transfer antar dompet

**Pemicu:** tombol (+) → Transfer.

1. Pilih dompet asal dan tujuan yang berbeda.
2. Isi nominal positif, tanggal, catatan opsional.
3. Konfirmasi, lalu Simpan sebagai satu operasi atomik.

**Sukses:** asal berkurang, tujuan bertambah, total gabungan tetap, riwayat menampilkan satu transfer yang bisa dipahami. **Laporan dan grafik pemasukan/pengeluaran tidak berubah.**

**State lain:** asal=tujuan, nominal tidak valid, atau transaksi gagal harus tidak menyimpan transfer separuh.

## F-05 — Temukan, edit, atau hapus transaksi

**Pemicu:** buka tab Transaksi.

1. Lihat urutan transaksi dan cari dengan kata kunci atau filter dasar.
2. Pilih item → Detail; pengguna dapat memilih Edit atau Hapus.
3. Edit: tampilkan nilai sebelumnya, validasi, lalu simpan perubahan.
4. Hapus: tampilkan konfirmasi, lalu hapus seluruh efek logis transaksi/transfer.

**Sukses:** riwayat, saldo, ringkasan, dan grafik menampilkan hasil baru secara konsisten.

**State lain:** tidak ada hasil → empty state; dibatalkan → data tetap sama; gagal → data tidak berubah sebagian.

## F-06 — Pantau Beranda dan dompet

**Pemicu:** buka aplikasi setelah minimal satu dompet dibuat.

Beranda minimal menampilkan **total saldo saat ini, daftar/ringkasan dompet, dan transaksi terbaru**. Tombol (+) mudah ditemukan. Pengguna bisa membuka daftar/detail dompet atau aktivitas.

**Sukses:** angka di Beranda sama dengan hasil ledger yang dipakai laporan transaksi. Tidak perlu banner promosi, fitur Scan, atau gamification dari gambar referensi.

## F-07 — Lihat laporan dan grafik

**Pemicu:** tab Laporan.

1. Lihat total pemasukan, pengeluaran, dan selisih.
2. Pilih periode harian/mingguan/bulanan (default bulanan) dan filter semua dompet/satu dompet.
3. Grafik batang membandingkan pemasukan dan pengeluaran sesuai periode.

**Sukses:** angka kartu ringkasan sesuai data grafik dan transaksi terkait. Transfer dan saldo awal tidak dihitung sebagai pemasukan/pengeluaran.

**State lain:** tidak ada data → `Rp0` + empty state informatif; pembaca layar dapat membaca nilai tanpa mengandalkan warna batang.

## F-08 — Backup data

**Pemicu:** Pengaturan → Backup.

1. Pengguna memilih aksi backup, membuat/memasukkan kata sandi backup.
2. Memilih tujuan berkas Android melalui SAF.
3. Aplikasi membuat backup terenkripsi.
4. Tampilkan pesan sukses/gagal yang jelas tanpa membocorkan password.

**Sukses:** berkas portabel dapat dibuka untuk restore pada instalasi baru dengan password benar. Jangan menyatakan backup aman bila belum dites restore.

## F-09 — Restore data

**Pemicu:** Pengaturan → Restore.

1. Pilih berkas backup melalui SAF dan masukkan password.
2. Validasi berkas/versi serta autentikasi/dekripsi tanpa mengganti database aktif.
3. Tampilkan konsekuensi restore dan minta konfirmasi eksplisit.
4. Pulihkan data secara aman; hitung ulang saldo dan ringkasan.

**Sukses:** dompet, kategori, transaksi, saldo, laporan, dan grafik konsisten dengan berkas backup.

**State lain:** batal/password salah/berkas rusak/skema tak didukung/pemulihan gagal → data lama tetap aman.

## F-10 — Kelola dompet dan kategori

- Tambah/ubah nama dompet. Dompet tidak memiliki aksi arsip pada MVP.
- Tambah/ubah kategori pemasukan/pengeluaran; pilih hanya kategori sesuai jenis transaksi.
- Jangan menghapus kategori atau dompet terpakai dengan cascade yang menghilangkan riwayat.
- Dompet yang memiliki transaksi tetap tersedia dan tidak boleh dihapus cascading bersama riwayat.
- Saldo awal boleh diedit setelah transaksi; jelaskan bahwa saldo terhitung berubah dan minta konfirmasi sebelum menyimpan.

## Kriteria selesai lintas alur

- Semua alur inti berjalan saat **airplane mode**.
- Proses simpan memberi feedback dan tidak membuat duplikat saat tombol ditekan berulang.
- Tombol back/batal tidak menyimpan perubahan tanpa konfirmasi.
- Status kosong, input invalid, dan kegagalan file/database terkomunikasikan dengan jelas.
- UI bisa digunakan dengan font besar dan pembaca layar; target sentuh minimal 48dp.
- Fitur di luar MVP **tidak muncul sebagai aksi yang seolah berfungsi**.
