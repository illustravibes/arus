# BUSINESS_RULES — Aturan Bisnis Arus MVP

> Aturan domain untuk UI, repository, database, dan testing. Dokumen ini memperinci [PRD.md](PRD.md); bila scope bertentangan dengan PRD, berhenti dan laporkan konflik. Tidak perlu menyalin domain kompleks Arus lama.

## 1. Model domain minimum

| Entitas konseptual | Isi minimum | Relasi/tujuan |
|---|---|---|
| Wallet / Dompet | ID, nama, saldo awal | Memiliki transaksi; menampilkan saldo terhitung. Dompet tidak diarsipkan pada MVP. |
| Category / Kategori | ID, nama, jenis INCOME/EXPENSE, status aktif/arsip | Digunakan untuk mengelompokkan transaksi sesuai jenis. |
| Transaction / Transaksi | ID, jenis, nilai rupiah, tanggal, dompet, kategori jika relevan, catatan opsional | Sumber kebenaran pemasukan/pengeluaran. |
| Transfer / Transfer | ID operasi, dompet asal, dompet tujuan, nilai rupiah, tanggal, catatan opsional | Satu operasi logis yang memengaruhi **dua** dompet atomik. |
| Backup | Format berversi, metadata kriptografi, payload terenkripsi | Salinan portabel data aplikasi untuk pemulihan. |

Ini adalah **model konseptual**, bukan perintah membuat lima tabel tertentu. Pilih representasi Room paling sederhana yang menjaga integritas data, termasuk dua efek transfer dan keterlacakan hubungan operasi.

## 2. Nilai uang, waktu, dan validasi

- Satu mata uang: **IDR**, bilangan bulat `Long` dalam rupiah. Tidak gunakan `Float`/`Double` untuk nilai uang atau agregasi.
- Setiap pemasukan, pengeluaran, dan transfer harus bernilai **lebih dari Rp0**; validasi angka, batas penyimpanan `Long`, dan overflow penjumlahan.
- Catatan bersifat opsional. Pemasukan/pengeluaran membutuhkan satu dompet dan satu kategori dengan jenis yang sesuai.
- Transfer membutuhkan dua **dompet berbeda**, nilai positif, dan tanggal.
- Simpan tanggal kejadian secara konsisten. Tanggal/hari dan batas periode diinterpretasikan menurut **zona waktu lokal perangkat** sesuai keputusan PRD; satu query dan satu grafik harus memakai batas yang sama.
- Transaksi tanggal **masa depan** boleh disimpan, tetapi efeknya belum dihitung sebagai saldo saat ini, ringkasan, atau realisasi laporan sebelum tanggal tersebut efektif.

## 3. Perhitungan saldo

```text
saldo_saat_ini(dompet) = saldo_awal
  + seluruh pemasukan efektif ke dompet
  - seluruh pengeluaran efektif dari dompet
  + seluruh transfer efektif masuk
  - seluruh transfer efektif keluar
```

- Database transaksi adalah **satu-satunya source of truth**. Nilai saldo dapat dihitung saat query/observasi; jangan mempunyai saldo mutable lain yang bisa berbeda dari ledger.
- Total saldo gabungan = jumlah saldo semua dompet. Karena dompet tidak diarsipkan pada MVP, semua dompet tetap terlihat dan diperhitungkan.
- Saldo negatif diperbolehkan. Pengeluaran atau transfer keluar tetap dapat dicatat meskipun saldo dompet menjadi di bawah Rp0.
- **Saldo awal bukan pemasukan** dan tidak masuk grafik/laporan.
- Perubahan transaksi harus langsung tercermin di saldo yang ditampilkan; query/Flow harus reaktif.

## 4. Aturan berdasarkan jenis transaksi

| Operasi | Efek dompet | Laporan pemasukan | Laporan pengeluaran |
|---|---|---|---|
| Pemasukan RpX ke A | A +X | +X | 0 |
| Pengeluaran RpX dari A | A −X | 0 | +X |
| Transfer RpX dari A ke B | A −X, B +X | 0 | 0 |
| Saldo awal A RpX | Basis A +X | 0 | 0 |
| Transaksi bertanggal masa depan | Belum ada efek pada saldo saat ini | Belum masuk realisasi | Belum masuk realisasi |

**Transfer** wajib menggunakan transaksi database atomik (`withTransaction` atau cara setara): membuat/mengubah/menghapus kedua efek sekaligus. Tidak boleh tersisa hanya sisi masuk atau keluar jika operasi gagal.

## 5. Edit, hapus, arsip, dan referensi

- Edit pemasukan/pengeluaran dapat mengganti nilai, tanggal, dompet, kategori valid, dan catatan; hitung ulang semua dampaknya.
- Edit transfer harus memutakhirkan asal, tujuan, jumlah, atau tanggal sebagai **satu operasi atomik**. Tidak boleh mengedit satu sisi transfer secara terpisah.
- Hapus transaksi/transfer meminta konfirmasi, lalu menghapus/menonaktifkan seluruh efek logis dan memperbarui saldo serta laporan.
- Kategori yang sudah memiliki transaksi **tidak boleh dihapus secara cascading** sehingga riwayat kehilangan arti. Gunakan arsip/nonaktif atau strategi aman yang mempertahankan label historis.
- Dompet yang memiliki riwayat **tidak boleh dihapus cascading** bersama transaksi. Dompet tidak diarsipkan pada MVP dan tetap tersedia agar histori serta saldo dapat dilihat.
- Saldo awal boleh diedit setelah ada transaksi. Perubahan ini mengubah saldo terhitung untuk dompet, sedangkan transaksi historis tidak diubah; UI perlu menjelaskan konsekuensi dan meminta konfirmasi sebelum menyimpan perubahan.

## 6. Laporan dan grafik

- Hanya transaksi **INCOME** dan **EXPENSE** yang efektif, pada periode dan filter terpilih, yang dihitung.
- Total pemasukan = jumlah pemasukan dalam filter; total pengeluaran = jumlah pengeluaran dalam filter; selisih = pemasukan − pengeluaran.
- Grafik batang memuat dua seri, pemasukan dan pengeluaran. Periode: harian, mingguan, bulanan (default bulanan); filter: semua dompet / satu dompet.
- Transfer **tidak** dihitung sebagai pemasukan/pengeluaran, baik saat filter semua dompet maupun dompet tunggal. Jika pengguna ingin melihat mutasi transfer, tampilkan dalam riwayat, bukan sebagai pendapatan/pengeluaran.
- Periode tanpa transaksi menunjukkan nilai `Rp0` (bukan error atau angka null). Ringkasan angka harus sama dengan penjumlahan data batang yang relevan.
- Gunakan batas rentang waktu **inklusif di awal, eksklusif di akhir** untuk menghindari hitung ganda. Minggu dimulai **Senin** sesuai keputusan PRD.
- Jangan mencampurkan perhitungan *net cash flow* (`income - expense`) dengan **saldo keseluruhan** (yang juga mencakup saldo awal dan transfer antardompet).

## 7. Contoh bisnis yang wajib menjadi test

Pada awal periode:

```text
Dompet Tunai:  Rp1.000.000 (saldo awal)
Dompet Bank:   Rp2.000.000 (saldo awal)

1. Pemasukan Rp500.000 ke Tunai
2. Pengeluaran Rp200.000 dari Tunai
3. Transfer Rp300.000 dari Tunai ke Bank
```

Hasil yang benar:

| Indikator | Nilai |
|---|---:|
| Saldo Tunai | Rp1.000.000 |
| Saldo Bank | Rp2.300.000 |
| Total saldo | Rp3.300.000 |
| Pemasukan periode | Rp500.000 |
| Pengeluaran periode | Rp200.000 |
| Selisih periode | Rp300.000 |

Transfer tidak menambah pemasukan dan saldo awal tidak muncul sebagai pemasukan. Menghapus transfer pada langkah 3 harus membuat saldo Tunai Rp1.300.000 dan Bank Rp2.000.000, tanpa mengubah laporan pemasukan/pengeluaran.

## 8. Backup dan restore

- Backup lokal dipilih/disimpan pengguna melalui **Storage Access Framework**; data disandikan dengan **AES-GCM** memakai kunci hasil derivasi password pengguna, dengan salt dan nonce unik serta format backup berversi.
- Backup dapat dipulihkan di perangkat lain dengan password benar; **jangan bergantung hanya pada Android Keystore perangkat asal** untuk berkas portabel.
- Restore: pilih file → masukkan password → validasi format/versi dan autentikasi → minta konfirmasi penggantian data → terapkan secara aman → verifikasi.
- Jika password salah, berkas rusak, skema tidak didukung, atau restore gagal, **jangan menghilangkan data aktif**.
- Setelah restore berhasil, semua saldo/grafik dihitung ulang dari data hasil restore; tidak boleh menyisakan cache lama.

## 9. Keputusan bisnis yang sudah ditetapkan

Keputusan pemilik proyek (7 November 2026):

1. **Saldo negatif:** diperbolehkan; pengeluaran/transfer tidak ditolak hanya karena saldo tidak cukup.
2. **Saldo awal setelah dipakai:** boleh diedit; perubahan memengaruhi saldo terhitung dan memerlukan penjelasan serta konfirmasi.
3. **Arsip dompet:** tidak ada pada MVP; dompet tetap tersedia.

Kategori yang sudah direferensikan transaksi tidak boleh dihapus cascading; pertahankan label historis. Aturan pengelolaan kategori tetap mengikuti baseline MVP.

## 10. Checklist test domain minimal

- [ ] Nominal positif; nilai invalid dan overflow ditolak.
- [ ] Saldo awal, pemasukan, pengeluaran, transaksi masa depan.
- [ ] Transfer beda dompet, atomisitas, tidak merubah total gabungan.
- [ ] Edit/hapus tiap jenis transaksi menjaga hasil saldo/laporan.
- [ ] Filter laporan per dompet dan tanggal, termasuk periode kosong.
- [ ] Agregasi grafik tepat sama dengan transaksi sumber.
- [ ] Kategori tidak salah jenis dan data historis tidak terhapus cascading.
- [ ] Backup→restore berhasil pada instalasi baru, salah password/berkas rusak tetap aman.
