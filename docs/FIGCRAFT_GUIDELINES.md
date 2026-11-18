# FIGCRAFT GUIDELINES — ARUS MOBILE DESIGN SYSTEM & STYLING SPECIFICATION

> **Status: Draft historis.** Dokumen ini tidak menjadi acuan pengembangan dan belum diselaraskan dengan desain terbaru. Spesifikasi, warna, dan aturan di bawah merupakan eksplorasi lama, bukan keputusan yang mengikat.
>
> Pengembangan UI mengikuti [DESIGN.md](../DESIGN.md) sebagai sumber kebenaran visual, bersama PRD dan dokumen teknis. Pekerjaan Figcraft tidak menjadi fokus saat ini.

---

## 1. Filosofi Desain

Semua screen di Figcraft dibangun dengan pendekatan **Mobile-First, Comfortable Density, dan Hybrid Auto Layout**.

* **Karakter Visual:** *Light, friendly, rounded, modular, content-first, playful in moderation*.
* **Less Noise, More Control:** Fokus utama pada kemudahan mencatat transaksi dan kejelasan nominal keuangan tanpa elemen dekoratif berlebih (tanpa heavy drop shadow, tanpa glassmorphism, tanpa gradien mencolok).
* **Referensi Utama:**
  * **Samsung One UI:** Pembagian area ergonomis *Viewing Area* (area pandang lapang) dan *Interaction Area* (zona interaksi jangkauan jempol).
  * **Notion & Gojek:** Hierarki informasi bersih, tenang, namun fungsional dan ramah pengguna.
  * **Arus Design System:** Identitas khas dengan warna latar `#F6F8FE`, kartu putih `#FFFFFF`, dan aksen biru elektrik `#0009FF`.

---

## 2. Canvas & Struktur Layar (One UI Layout Pattern)

* **Dimensi Canvas Standar:** `393 × 852 px` (Mobile Portrait standard, mis. iPhone 16 / Galaxy S series).
* **Corner Radius Canvas Screen:** `28–32 px` (Device frame preview).
* **Background Canvas:** `#F6F8FE` (`surface.page`).

```
┌─────────────────────────────────────────┐  y: 0
│ Status Bar (9:41, Icon sinyal & baterai) │  y: 13..28
├─────────────────────────────────────────┤
│ VIEWING AREA (Area Pandang / Header)    │
│ • Margin horizontal lega (20–24 px)     │  y: 44..120
│ • Open space impression (lapang, rileks)│
│ • Large title & kontekstual subheader   │
├─────────────────────────────────────────┤
│ MARGIN CONTRAST (Gap pemisah 20–24 px)  │
├─────────────────────────────────────────┤
│ INTERACTION AREA (Zona Operasi Bawah)   │
│ • Rentang jangkauan jempol (65–70%)     │  y: 140..740
│ • Kartu putih modular (radius 20–24 px) │
│ • Tombol aksi pill (touch target ≥48dp) │
│ • List transaksi density Comfortable    │
├─────────────────────────────────────────┤
│ BOTTOM NAVIGATION (Floating pill nav)   │  y: 760..826 (h: 64 px)
└─────────────────────────────────────────┘  y: 852
```

### Aturan Viewing Area vs Interaction Area:
1. **Viewing Area (Atas):**
   * Ditujukan untuk informasi pasif yang dipahami sekilas (sapaan, judul halaman, status ringkas).
   * **Dilarang meletakkan kontrol interaktif utama di sini** untuk menghindari jempol meregang ke atas layar.
   * Memiliki margin lebih lebar (*wide margins*) agar mata pengguna rileks sebelum masuk ke area data.
2. **Interaction Area (Bawah):**
   * Berada di area jangkauan jempol yang natural.
   * Menampung kartu saldo, tombol aksi transaksi, filter chip, form input, dan daftar riwayat.
   * Kontras dengan viewing area: area ini terstruktur padat-rapi menggunakan kartu putih (`#FFFFFF`) dengan border halus (`#E5E9F2`).

---

## 3. Token Warna (Color Tokens)

| Kategori Token | Token Name | Hex Code | Peran & Penggunaan di Figcraft |
|---|---|---|---|
| **Brand** | `brand.primary` | `#0009FF` | Biru elektrik khas Arus; CTA utama, tab aktif, link aksi penting. |
| | `brand.soft` | `#ECEEFF` | Background chip aktif, badge seleksi non-dominan. |
| **Surface** | `surface.page` | `#F6F8FE` | Latar belakang seluruh halaman/canvas. |
| | `surface.card` | `#FFFFFF` | Kontainer kartu, form field, sheet, dan panel bottom bar. |
| | `surface.subtle` | `#E8EFFE` | Latar badge netral, ikon tile informatif. |
| **Border** | `border.subtle` | `#E5E9F2` | Border kartu (1px solid), divider, dan stroke input. |
| **Text** | `text.primary` | `#34364A` | Judul, isi utama, angka nominal, teks tombol gelap. |
| | `text.secondary` | `#60657A` | Subjudul, label kecil, metadata tanggal/dompet, hint text. |
| **Finansial** | `finance.income` | `#087E56` | Teks & tanda pemasukan (`+ Rp ...`), ikon panah masuk. |
| | `finance.incomeSoft` | `#E3F8EE` | Latar tile/badge pemasukan. |
| | `finance.expense` | `#B7324C` | Teks & tanda pengeluaran (`− Rp ...`), ikon panah keluar. |
| | `finance.expenseSoft` | `#FDEAF0` | Latar tile/badge pengeluaran. |
| **State** | `state.warning` | `#986105` | Teks peringatan / perhatian. |
| | `state.warningSoft` | `#FFF4D7` | Latar badge peringatan. |

> [!NOTE]
> Pada kartu finansial atau list item, jangan hanya mengandalkan warna hijau/merah. Selalu sertakan tanda eksplisit (`+`, `−`), arah panah, atau label kategori untuk menjamin aksesibilitas.

---

## 4. Tipografi (Typography)

* **Font Family:** `Inter`, pengganti SF Pro pada referensi Bevel (fallback: `sans-serif`).
* **Format Angka Nominal:** Utamakan keterbacaan tinggi (*tabular figures*).

| Peran Tipografi | Ukuran (sp/px) | Weight | Line Height | Penggunaan |
|---|---|---|---|---|
| `viewingHeader` | 26–28 px | Bold | 34–36 px | Judul besar Viewing Area One UI (mis. *Selamat pagi, Rani 👋*, *Transaksi*). |
| `displayAmount` | 30–32 px | Bold | 38–40 px | Angka Total Saldo utama di kartu saldo. |
| `screenTitle` | 22–24 px | Bold | 30–32 px | Judul layar subhalaman (mis. *Catat Transaksi*). |
| `sectionTitle` | 18–20 px | Bold | 24–26 px | Judul seksi (*Aksi cepat*, *Ringkasan bulan ini*, *Aktivitas terbaru*). |
| `cardTitle` | 15–16 px | Semi Bold | 20–22 px | Judul kartu atau nama item transaksi. |
| `body` | 14–15 px | Regular | 20–22 px | Teks isi umum, label form, deskripsi. |
| `caption` | 12–13 px | Medium | 16–18 px | Label seksi kecil (*TOTAL SALDO*), metadata tanggal, nama dompet. |

---

## 5. Dimensi Komponen Standar (Component Sizes)

| Komponen | Tinggi / Ukuran | Corner Radius | Padding / Karakteristik |
|---|---|---|---|
| **Primary Button (CTA)** | 56 px | Full Pill (`100 px`) | Fill `#0009FF`, Teks Putih 15px Bold. |
| **Secondary / Pill Button** | 48–52 px | Full Pill (`100 px`) | Fill `#FFFFFF` / `#ECEEFF`, Stroke `#E5E9F2`. |
| **Action Tile (Quick Action)**| 80–88 px | `18–20 px` | Lebar seragam (3 kolom), Icon FA 24px + Teks 13px. |
| **Card Kontainer Utama** | Hug / Auto | `20–24 px` | Fill `#FFFFFF`, Stroke 1px `#E5E9F2`, Padding 16–20px. |
| **Transaction List Row** | 68–76 px | `16–18 px` | Icon tile 44×44px, Title 15px, Amount 15px Bold. |
| **Icon Tile Container** | 44 × 44 px | `14 px` | Soft tile (`#E3F8EE` / `#FDEAF0` / `#E8EFFE`). |
| **Input Nominal (Amount)** | 80–96 px | `20 px` | Text 28–32px Bold, visual dominan pada form transaksi. |
| **Text Input / Dropdown** | 56–60 px | `16–18 px` | Fill `#FFFFFF`, Stroke 1px `#E5E9F2`, Padding h: 16px. |
| **Filter Chip** | 40–44 px | Full Pill (`100 px`) | Padding h: 16px, v: 8px. Touch target ≥ 48dp. |
| **Bottom Navigation Bar** | 64 px | `24 px` (Pill float) | Lebar 369px (margin 12px), 4 tab item berjarak seimbang. |

---

## 6. Spacing Scale (4px System)

* `4 px`: Jarak mikro (gap ikon ke teks kecil di tab bar).
* `8 px`: Gap internal elemen berpasangan (judul ke subjudul kartu, ikon ke teks label).
* `12 px`: Jarak antarkartu kecil (gap grid 2 kolom / 3 kolom).
* `16 px`: Padding standar isi kartu & margin tepi nyaman.
* `20 px`: Padding internal kartu besar (Balance card) / margin horizontal layar One UI.
* `24 px`: Gap antarseksi utama (Section to section spacing).
* `32 px`: Pemisah visual besar (Viewing Area margin contrast ke kartu pertama).

---

## 7. Arsitektur Hybrid Auto Layout Figcraft

Setiap frame di Figcraft **wajib terstruktur rapi** menggunakan Auto Layout bersarang (*nested Auto Layout*) tanpa elemen mengambang bebas (*absolute*) yang tidak disengaja.

### Aturan Ukuran (Sizing Rules):
* **Fixed:**
  * Tinggi tombol (`56px`), tinggi input (`56px`), dimensi ikon container (`44×44px`), tinggi row (`72px`), tinggi tab bar (`64px`).
* **Hug Contents:**
  * Teks judul, label, badge nominal, isi kartu yang tingginya dinamis mengikuti jumlah baris.
* **Fill Container:**
  * Kontainer kolom utama (`Beranda content`), lebar kartu di dalam kolom, lebar input field, nama transaksi (agar mengisi sisa ruang baris).

### Template Hierarki Layer (Contoh: Screen 01 Beranda):
```
01 Beranda [Frame 393 × 852, Fill #F6F8FE, Corner: 28px]
├── Status bar [Horizontal Auto Layout, Fixed H: 44px, Space Between]
│   ├── Status time [Hug]
│   └── Status icons [Hug]
│
├── Content Column [Vertical Auto Layout, W: 353px (Fill), Gap: 20–24px]
│   ├── Viewing Area: Greeting Header [Vertical, Gap: 6px]
│   │   ├── Greeting Title ("Selamat pagi, Rani 👋") [Hug, 26px Bold]
│   │   └── Greeting Subtitle ("Ini ringkasan keuanganmu hari ini") [Hug, 14px Regular]
│   │
│   ├── Interaction Area: Balance Card [Vertical, Fill W, Corner: 24px, Pad: 20px, Stroke: 1px]
│   │   ├── Balance Label ("TOTAL SALDO") [Hug, 12px Medium]
│   │   ├── Balance Amount ("Rp 8.450.000") [Hug, 30px Bold]
│   │   └── Balance Meta ("Semua dompet · hari ini") [Hug, 13px]
│   │
│   ├── Interaction Area: Quick Actions Section [Vertical, Gap: 10px]
│   │   ├── Section Title ("Aksi cepat") [Hug, 18px Bold]
│   │   └── Actions Row [Horizontal, Gap: 12px, Space Between]
│   │       ├── Action Masuk [Vertical, Fixed H: 84px, Fill W, Corner: 18px]
│   │       ├── Action Keluar [Vertical, Fixed H: 84px, Fill W, Corner: 18px]
│   │       └── Action Transfer [Vertical, Fixed H: 84px, Fill W, Corner: 18px]
│   │
│   ├── Interaction Area: Monthly Summary Section [Vertical, Gap: 10px]
│   │   ├── Section Title ("Ringkasan bulan ini") [Hug, 18px Bold]
│   │   └── Summary 2-Col Grid [Horizontal, Gap: 14px]
│   │       ├── Income Card [Vertical, Fill W, Corner: 18px, Pad: 16px]
│   │       └── Expense Card [Vertical, Fill W, Corner: 18px, Pad: 16px]
│   │
│   └── Interaction Area: Recent Activity Section [Vertical, Gap: 10px]
│       ├── Section Title ("Aktivitas terbaru") [Hug, 18px Bold]
│       ├── Transaction Row 1 [Horizontal, Fixed H: 72px, Pad: 14px, Corner: 18px]
│       └── Transaction Row 2 [Horizontal, Fixed H: 72px, Pad: 14px, Corner: 18px]
│
└── Bottom Navigation [Horizontal, Float W: 369px, H: 64px, Corner: 24px, Space Between]
    ├── Tab Beranda [Vertical, Fill W, Center]
    ├── Tab Transaksi [Vertical, Fill W, Center]
    ├── Tab Laporan [Vertical, Fill W, Center]
    └── Tab Atur [Vertical, Fill W, Center]
```

---

## 8. Tingkat Kepadatan Visual (Visual Density)

1. **Comfortable (Default Aplikasi):**
   * Digunakan untuk Beranda, Form Catat Transaksi, Pengaturan, dan Modal Dialog.
   * Padding kartu `16–20px`, jarak item `12–16px`, tinggi row `72px`. Memberi kesan tenang dan mudah diakses.
2. **Compact (Hanya untuk Tampilan Sarat Data):**
   * Digunakan untuk Riwayat Transaksi panjang (`02 Transaksi`) dan Laporan Keuangan (`03 Laporan`).
   * Padding kartu `12px`, tinggi baris `56–64px`.
   * **Aturan Mutlak:** Jangan mengecilkan touch target di bawah `48×48 dp` atau memangkas teks esensial.
3. **Relaxed (Fokus & Emosional):**
   * Digunakan untuk Onboarding dompet pertama, Empty states (*Belum ada transaksi*), dan Layar Sukses.
   * Ruang kosong lebih banyak, ilustrasi/ikon berukuran lega.

---

## 9. Checklist Verifikasi Layar Sebelum Review (Quality Gate)

Sebelum mengajukan satu screen kepada pengguna untuk di-review:
- [ ] **Canvas:** Berukuran `393 × 852 px`, background `#F6F8FE`.
- [ ] **One UI Split:** Memiliki *Viewing Area* lapang di atas dan *Interaction Area* ergonomis di bawah.
- [ ] **Auto Layout:** Semua elemen berada dalam Auto Layout (Fixed, Hug, Fill diterapkan secara tepat). Tidak ada layer berantakan.
- [ ] **Touch Target:** Seluruh tombol, filter chip, dan baris klik minimal `48 × 48 dp`.
- [ ] **Token Warna:** Hanya memakai token palet resmi (`#0009FF`, `#34364A`, `#60657A`, `#E5E9F2`, `#087E56`, `#B7324C`).
- [ ] **Font & Sizing:** Tipografi Inter dengan skala yang terbaca jelas.
- [ ] **Preview Ter-export:** File screenshot high-res PNG tersimpan di `design/previews/` untuk inspeksi langsung.

