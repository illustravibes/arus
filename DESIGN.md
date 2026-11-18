# Arus — Design System

> Sumber kebenaran visual dan komponen UI Arus Android MVP. Referensi: karakter rounded dan terang dari Bevel, serta ilustrasi dan warna ekspresif dari Notion. Keduanya adalah inspirasi gaya, bukan spesifikasi fitur atau brand untuk disalin.

## Arah desain

**Rounded, airy, and calm like Bevel; illustrated, expressive, and personal like Notion.** Arus membantu orang memahami uangnya dengan cepat: informasi finansial harus terbaca lebih dulu, lalu detail visual memberi rasa hangat dan personal. Utamakan kartu putih rounded, ruang yang cukup, judul tegas, ikon dalam soft tile, CTA pill, dan daftar yang tenang. Gunakan warna dan ilustrasi sebagai aksen, bukan dekorasi di semua tempat.

Arus adalah aplikasi Android native, bukan landing page. Jangan mengadopsi hero foto, mockup perangkat, banner promosi, katalog, footer web, atau komposisi dan skala tipografi desktop dari referensi. Fitur dan perilaku tetap mengikuti PRD, aturan bisnis, dan alur pengguna.

## Warna

| Token | Nilai | Penggunaan |
|---|---|---|
| Background | `#F6F8FE` | Latar layar biru-abu terang |
| Surface | `#FFFFFF` | Kartu, sheet, dan permukaan konten |
| Text primary | `#34364A` | Judul, nominal, dan konten utama |
| Text secondary | `#60657A` | Label dan keterangan pendukung |
| Border | `#E5E9F2` | Pemisah dan batas kartu yang halus |
| Brand candidate | `#0009FF` | Kandidat aksi utama dan state aktif, setelah pengujian kontras |

Biru brand bukan kewajiban jika tidak lolos pengujian kontras pada pasangan warna dan state yang dipakai. Pilih warna pengganti yang lolos, lalu gunakan konsisten; jangan menganggap satu nilai aman di semua latar. Gunakan warna semantik yang konsisten untuk pemasukan, pengeluaran, error, dan sukses; jenis dan status juga harus disampaikan lewat teks atau ikon, bukan warna saja.

### Palet kategori

Palet pilihan pengguna terinspirasi dari accent Notion: Marigold `#FFB110`, Coral `#F64932`, Mocha `#B18164`, Signal Blue `#097FE8`, Sky `#62AEF0`, dan Midnight `#02093A`. Ini palet awal, bukan jaminan kontras. Gunakan warna secara konsisten pada ikon, tile, atau chip kategori; tint lembut boleh digunakan untuk latar. Tentukan warna foreground yang terbaca untuk setiap kombinasi dan uji kontras aktual, termasuk state terpilih. Nama kategori dan ikon tetap tersedia tanpa bergantung pada warna. Warna kategori tidak menentukan jenis pemasukan atau pengeluaran.

## Tipografi dan ruang

- Gunakan Inter sebagai pengganti SF Pro pada referensi Bevel, dengan fallback sans-serif. Judul tegas, label dan isi mudah dipindai; nominal menjadi fokus dan gunakan angka tabular bila tersedia.
- Ukuran mengikuti kebutuhan layar Android, tidak menyalin ukuran display situs referensi. Izinkan teks membesar dan membungkus saat font sistem diperbesar; jangan memotong nominal atau label penting.
- Padding layar dasar 16dp; gunakan jarak kelipatan 4dp dan whitespace terstruktur. Kepadatan harus tetap nyaman pada layar sempit.
- Target sentuh minimum 48dp, termasuk ikon dan kontrol kecil.

## Bentuk dan permukaan

Kartu utama menggunakan sudut besar dan lembut, sebagai titik awal 24–32dp; elemen di dalamnya dapat memakai radius lebih kecil. CTA utama dan chip memakai bentuk pill. Sesuaikan radius dengan ukuran komponen agar tidak mengganggu isi atau target sentuh. Gunakan border halus dan shadow minimal; bedakan permukaan terutama lewat warna dan jarak. Pertahankan konsistensi bentuk, bukan radius yang sama secara kaku pada semua komponen.

## Komponen dan interaksi

- **Tombol:** satu aksi primer yang jelas per layar, berbentuk pill; aksi sekunder menggunakan treatment yang lebih tenang. Tampilkan state enabled, disabled, pressed, dan focus yang terbaca.
- **Kartu:** permukaan putih rounded untuk saldo, dompet, ringkasan, dan kelompok konten. Angka dan label lebih dominan daripada hiasan.
- **Kategori:** ikon yang mudah dikenali dalam soft tile dan nama kategori; chip pill dapat memakai warna pilihan pengguna. Pilihan aktif juga ditandai dengan ikon atau label, bukan warna saja.
- **Daftar transaksi:** tampilkan hierarki nama/kategori, keterangan relevan, dan nominal dengan jelas. Bedakan pemasukan, pengeluaran, dan transfer lewat teks atau ikon selain warna.
- **Form, dialog, dan sheet:** gunakan Material 3 untuk perilaku, validasi, aksesibilitas, dan state. Jelaskan error di dekat input; konfirmasi tindakan destruktif sesuai alur.
- **Grafik:** batang rounded boleh digunakan jika tidak mengurangi ketepatan pembacaan. Sertakan label, legenda, serta nilai ringkasan dalam teks; transfer tidak masuk seri pemasukan/pengeluaran.
- **Komponen bersama:** beri nama `Arus*` ketika benar-benar digunakan berulang; hindari abstraksi prematur.

## Ilustrasi, gerak, dan bahasa

Gunakan ilustrasi flat sederhana dengan karakter orisinal, garis tangan, atau sparkle kecil sebagai tanda baca visual. Tempat paling tepat adalah empty state, onboarding, atau konfirmasi sukses. Ilustrasi tidak boleh menyerupai aset atau maskot Notion secara literal, menghalangi data, atau menjadi satu-satunya pembawa informasi. Tidak perlu ilustrasi di setiap kartu.

Gerak singkat dan halus dapat memberi respons pada pemilihan atau keberhasilan; sekitar 200ms adalah titik awal, bukan aturan kaku. Spring ringan hanya untuk detail ilustratif yang relevan. Hormati pengaturan pengurangan animasi perangkat dan jangan menunda pekerjaan pengguna.

Gunakan bahasa Indonesia yang hangat dan jelas: “Yuk, buat dompet pertama” atau “Belum ada transaksi”. Pada error, nominal, penghapusan, dan restore, prioritaskan kejelasan konsekuensi daripada gaya bercanda.

## Penerapan per layar

- **Beranda:** saldo sebagai fokus; kartu dompet dan aktivitas terbaru mudah dipindai. Empty state mengarahkan ke langkah berikutnya.
- **Transaksi:** daftar tenang dengan ikon kategori, nominal jelas, pencarian dan filter; sediakan empty state yang sesuai.
- **Laporan:** ringkasan dan grafik batang terbaca bersama legenda, periode, filter dompet, serta nilai teks. Periode kosong menampilkan Rp0 sesuai aturan bisnis.
- **Pengaturan:** kelompokkan dompet, kategori, backup/restore, dan preferensi dengan label langsung dan navigasi jelas.
- **Catat, detail, dan edit:** fokus pada isi dan satu aksi primer; validasi terlihat, tombol kembali jelas, dan konfirmasi tersedia sesuai alur.

Empat tab utama adalah Beranda, Transaksi, Laporan, dan Pengaturan; tombol (+) membuka pemasukan, pengeluaran, atau transfer. Subhalaman memiliki navigasi kembali dan tanpa bottom bar secara default.

## Aksesibilitas dan verifikasi

Sediakan loading, empty, error, dan success state sesuai konteks. Uji kontras teks dan komponen terhadap latar aktual, termasuk warna kategori dan state interaktif; dukung TalkBack, urutan fokus, label kontrol, font besar, serta layar sempit. Verifikasi pada emulator atau perangkat sebelum mengklaim aksesibilitas atau kesesuaian visual. Desain ini tidak mengubah aturan keuangan, cakupan MVP, atau persyaratan keamanan.
