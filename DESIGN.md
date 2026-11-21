# Arus — Design System

> Sumber kebenaran visual dan komponen UI Arus Android MVP. Referensi: karakter rounded dan terang dari Bevel, serta ilustrasi dan warna ekspresif dari Notion. Keduanya adalah inspirasi gaya, bukan spesifikasi fitur atau brand untuk disalin.

## Arah desain

**Rounded, airy, and calm like Bevel; illustrated, expressive, and personal like Notion.** Arus membantu orang memahami uangnya dengan cepat: informasi finansial harus terbaca lebih dulu, lalu detail visual memberi rasa hangat dan personal. Utamakan kartu putih rounded, ruang yang cukup, judul tegas, ikon dalam soft tile, CTA pill, dan daftar yang tenang. Gunakan warna dan ilustrasi sebagai aksen, bukan dekorasi di semua tempat.

Arus adalah aplikasi Android native, bukan landing page. Jangan mengadopsi hero foto, mockup perangkat, banner promosi, katalog, footer web, atau komposisi dan skala tipografi desktop dari referensi. Fitur dan perilaku tetap mengikuti PRD, aturan bisnis, dan alur pengguna.

## Lima prinsip design system (Bevel + Notion, diputuskan 2026-11-21)

Hasil audit 587 screenshot referensi lokal (`design/bevel` + `design/notion`) dan semua keputusan user 2026-11-18 s.d. 2026-11-21. Detail audit per komponen ada di rencana design system di `workspace/goals/arus-ready-to-production/files/rencana-design-system-bevel-notion-2026-11-21.md`.

1. **Struktur ikut Bevel.** Satu kartu putih = satu blok konten; kartu-kartu terpisah dengan gap (bukan hairline antar kartu). Radius kartu 16dp, tanpa bayangan berat.
2. **Pengelompokan di dalam kartu ikut Notion.** Di dalam satu kartu, baris-baris dipisah hairline tipis (`HorizontalDivider`, `ArusBorder`, 1dp): satu kartu per hari, "Kelola Data", Kelola Dompet/Kategori, action sheet; form Catat memakai satu kartu per field + label di atasnya.
3. **CTA ikut Bevel, warna hitam.** Primary = pill hitam penuh `#1A1A1A`, teks putih Bold. Biru Notion tidak dipakai sebagai warna aksi.
4. **Warna isi ikut palet Notion.** Kategori, tile ikon, dan status memakai palet Notion; semantik uang tetap hijau `#00A86B` (masuk) / merah `#E53935` (keluar).
5. **Sheet & menu ikut Notion, tombol sheet ikut Bevel.** Bottom sheet: judul tengah + opsi dalam kartu + drag handle pill 36×4dp. Tombol di bawah sheet: primary hitam penuh + tombol teks di bawahnya.

## Aturan pola (jangan buat pola baru)

- **Satu kartu per kelompok:** setiap daftar berkelompok = satu `ArusCard` + divider antar baris — kecuali form Catat: satu kartu per field + label di atasnya (keputusan user 2026-11-21).
- **Hitam-putih murni (2026-11-21):** latar `#FFFFFF`, kartu `#FFFFFF` (pemisah = border 1dp `#E5E5E5`), teks `#1A1A1A`/`#616161`, abu pendukung `#F0F0F0`; tidak ada tint biru di latar/kartu/border. Tile ikon warna (palet Notion) tetap sebagai aksen kategori.
- **Chip:** terpilih = pill hitam; tidak terpilih = latar `ArusBrandSoft` tanpa border.
- **Baris transaksi:** judul = catatan (dominan), subtitle = `dompet • kategori • jam` (pemisah dot).
- **Chart Laporan:** batang hijau/merah (semantik uang); label sumbu-Y di kanan (format ringkas "3 jt"/"250 rb"); batang terpilih opacity penuh, yang lain 0.45.
- **Snackbar:** `ArusSnackbar` — pill hitam + centang putih + aksi "Lihat" putih Bold, di atas bottom nav (bottom 160dp).
- **Detail sheet transaksi:** "Ubah" hitam penuh + "Hapus" teks merah di bawahnya; "Tutup" via drag/scrim/back (tanpa tombol).
- **Sheet chrome (semua `ModalBottomSheet`):** `skipPartiallyExpanded = true`, radius atas 28dp, `ArusSurface`, drag handle pill.
- **Action sheet (menu •••):** bottom sheet ala Notion — judul = nama item (tengah), opsi dalam satu kartu + hairline, ikon kiri + teks, destruktif merah.
- **Selection sheet (dropdown pilih dompet/kategori):** full rounded — keempat sudut 28dp + mengambang 16dp dari bawah (keputusan user 2026-11-21); sheet lain tetap sudut atas 28dp menempel bawah.
- **Selector row:** nilai yang dipilih di kiri, chevron tipis 12dp di kanan; baris navigasi (Kelola Data) memakai pola normal (label kiri + nilai + chevron).

## Warna

| Token | Nilai | Penggunaan |
|---|---|---|
| Background | `#FFFFFF` | Latar layar putih murni |
| Surface | `#FFFFFF` | Kartu, sheet, dan permukaan konten |
| Text primary | `#1A1A1A` | Judul, nominal, dan konten utama |
| Text secondary | `#616161` | Label dan keterangan pendukung |
| Border | `#E5E5E5` | Pemisah dan batas kartu yang halus |
| Brand primary | `#1A1A1A` | Aksi utama (CTA hitam netral); biru `#0009FF` ditolak user 2026-11-18 |
| Brand soft | `#F0F0F0` | Latar field search, chip tak terpilih |
| Pemasukan | `#00A86B` | Semantik uang masuk (soft `#E3F8EE`) |
| Pengeluaran | `#E53935` | Semantik uang keluar (soft `#FDEAF0`) |

Primer hitam netral `#1A1A1A` adalah keputusan final user (2026-11-18, "Tetap hitam"); biru terang `#0009FF` ditolak dan tidak dipakai sebagai warna aksi di mana pun. Latar `#FFFFFF` putih murni (keputusan user 2026-11-21 — tanpa tint biru, menggantikan `#F6F8FE`); kartu putih dibedakan dari latar oleh border 1dp `#E5E5E5`. Biru Notion hanya hidup sebagai warna kategori/ikon (Notion Sky). Gunakan warna semantik yang konsisten untuk pemasukan, pengeluaran, error, dan sukses; jenis dan status juga harus disampaikan lewat teks atau ikon, bukan warna saja.

### Palet kategori

Palet kategori memakai accent Notion (token di `ArusTheme.kt`): Marigold `#FFB110`, Coral `#F64932`, Sky `#62AEF0`, Mocha `#B18164`, Mint `#00A86B`, Lavender `#8B5CF6`, masing-masing dengan versi soft (`#FFF7E6`, `#FEECE9`, `#EFF7FF`, `#F7F2EF`, `#E3F8EE`, `#F3E8FF`) untuk tile ikon. Gunakan warna secara konsisten pada ikon, tile, atau chip kategori. Nama kategori dan ikon tetap tersedia tanpa bergantung pada warna. Warna kategori tidak menentukan jenis pemasukan atau pengeluaran.

## Tipografi dan ruang

- Gunakan Inter sebagai pengganti SF Pro pada referensi Bevel, dengan fallback sans-serif. Judul tegas, label dan isi mudah dipindai; nominal menjadi fokus dan gunakan angka tabular bila tersedia.
- Ukuran mengikuti kebutuhan layar Android, tidak menyalin ukuran display situs referensi. Izinkan teks membesar dan membungkus saat font sistem diperbesar; jangan memotong nominal atau label penting.
- Padding layar dasar 12dp (compact rapat 40%, keputusan user 2026-11-21); gap antar section 16dp; spacer bawah layar tab 110dp; gunakan jarak kelipatan 4dp. Kepadatan harus tetap nyaman pada layar sempit.
- Label dan judul memakai sentence case (bukan ALL-CAPS); tidak ada `.uppercase()` di komponen UI. Pengecualian: nama merek (BCA, DANA, OCBC) dan kode tipe data.
- Target sentuh minimum 48dp, termasuk ikon dan kontrol kecil.

## Bentuk dan permukaan

Kartu utama (`ArusCard`) memakai radius 16dp dan padding 12dp, border 1dp `ArusBorder`, tanpa bayangan berat. CTA utama dan chip memakai bentuk pill. Field input memakai pill penuh (keputusan user); satu-satunya pengecualian adalah field search (`ArusSearchField`: radius 12dp, latar `ArusBrandSoft`, tanpa border). Sesuaikan radius dengan ukuran komponen agar tidak mengganggu isi atau target sentuh. Bedakan permukaan terutama lewat warna dan jarak.

## Komponen dan interaksi

- **Tombol:** satu aksi primer yang jelas per layar, berbentuk pill; aksi sekunder menggunakan treatment yang lebih tenang. Tampilkan state enabled, disabled, pressed, dan focus yang terbaca.
- **Kartu:** permukaan putih rounded untuk saldo, dompet, ringkasan, dan kelompok konten. Angka dan label lebih dominan daripada hiasan.
- **Kategori:** ikon yang mudah dikenali dalam soft tile dan nama kategori; chip pill dapat memakai warna pilihan pengguna. Pilihan aktif juga ditandai dengan ikon atau label, bukan warna saja.
- **Daftar transaksi:** tampilkan hierarki nama/kategori, keterangan relevan, dan nominal dengan jelas. Bedakan pemasukan, pengeluaran, dan transfer lewat teks atau ikon selain warna.
- **Form, dialog, dan sheet:** gunakan Material 3 untuk perilaku, validasi, aksesibilitas, dan state. Jelaskan error di dekat input; konfirmasi tindakan destruktif sesuai alur.
- **Grafik:** batang rounded boleh digunakan jika tidak mengurangi ketepatan pembacaan. Sertakan label sumbu-Y di kanan (format ringkas "3 jt"/"250 rb"), legenda, serta nilai ringkasan dalam teks; batang terpilih ditandai opacity penuh vs 0.45; transfer tidak masuk seri pemasukan/pengeluaran.
- **Snackbar:** `ArusSnackbar` (pill hitam + centang putih + aksi "Lihat" putih Bold) untuk sukses simpan; posisi tetap di atas bottom nav (bottom 160dp).
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
