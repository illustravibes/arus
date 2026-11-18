# Audit Desain Hi-Fi Arus

Tanggal: 9 November 2026  
Dokumen yang ditinjau: `design/Arus-HiFi.figcraft`

Audit visual mencakup frame Beranda, Transaksi, Laporan, Pengaturan, Catat Transaksi, dan Detail Transaksi. File desain tidak diubah sebagai bagian dari audit ini.

## Ringkasan

Arah visual sudah konsisten dengan konsep comfortable UI: latar lembut, kartu yang lapang, teks jelas, dan nominal mudah ditemukan. Informasi kontekstual cukup kaya tanpa membuat layar terasa penuh.

## Hal yang sudah kuat

- Beranda memiliki hierarki yang mudah dipindai: saldo, aksi cepat, ringkasan, lalu aktivitas terbaru. Tiga aksi inti terlihat jelas.
- Baris transaksi menampilkan nama, dompet/kategori, dan nominal. Transfer dibedakan dari pemasukan dan pengeluaran.
- Form pencatatan menonjolkan nominal, lalu menyusun dompet, kategori, tanggal, dan catatan secara berurutan dengan CTA yang mudah ditemukan.
- Detail transaksi menyatukan nominal dan informasi pendukung. Aksi ubah dan hapus tampak jelas.
- Laporan menampilkan angka bersama grafik dan legenda pemasukan/pengeluaran.

## Prioritas perbaikan

1. Tambahkan bagian dompet di Beranda. Saldo total belum menjawab berapa saldo pada tiap dompet, padahal saldo per dompet adalah kebutuhan inti Arus.
2. Perjelas cara memulai transaksi dari layar Transaksi. Pada frame yang ditinjau, tombol tambah tidak terlihat; aksi (+) sebaiknya mudah dijangkau dari layar ini.
3. Samakan navigasi bawah antarframe. Ikon tab tampak pada Beranda, tetapi di beberapa layar lain terlihat sebagai titik. Label “Atur” juga berbeda dari “Pengaturan” pada dokumen produk.
4. Lengkapi state yang belum terlihat: belum ada dompet/transaksi pertama, hasil pencarian kosong, validasi nominal, dan konfirmasi hapus.
5. Tinjau Laporan pada ukuran sempit dan dengan data nyata. Ringkasan tiga kolom terlihat rapat; periksa nominal panjang, label periode, serta kesamaan rentang/filter antara ringkasan dan grafik.

## Batas audit

Audit ini hanya menilai frame desain yang tersedia. Desain belum diuji di perangkat nyata. Langkah desain yang disarankan adalah memperbarui Beranda dan navigasi Transaksi, lalu menambahkan frame empty state dan dialog konfirmasi hapus.
