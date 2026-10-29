# Expo UX Plan — Arus Android

## Dasar

Wireframe `wireframes/*.dc.html` berukuran 390×844 dan menggunakan gaya sketsa dengan border tebal. Hierarchy, urutan konten, dan screen code dipertahankan; visual production diperbaiki menjadi Material 3 Android yang tenang, padat, dan mudah dipindai.

## Pemetaan utama

| Kebutuhan wireframe | Primitive yang dipilih |
|---|---|
| Bottom navigation | Expo Router tabs dengan Material-style navigation bar |
| Quick add | FAB dan `@expo/ui` bottom sheet |
| Picker wallet/category | Native picker/search sheet; fallback RN jika API tidak cukup |
| Form transaction | RN layout + `@expo/ui` TextField/DateTimePicker/segmented controls |
| History | FlatList dengan day sections dan stable key |
| Feedback | Snackbar untuk undo; AlertDialog untuk destructive confirmation |
| Budget/progress | Native progress indicator atau accessible RN equivalent |

## Prinsip perbaikan

- Ganti font playful dan shadow hitam wireframe dengan system typography, tonal surfaces, dan elevation ringan.
- Pertahankan bahasa Indonesia, angka Rupiah yang konsisten, dan hierarchy net worth → wallets → action inbox → budgets → recent transactions.
- Setiap layar memiliki loading, empty, validation, error/retry, dan offline state yang sesuai.
- Semua angka yang menjadi dasar keputusan dapat diketuk untuk membuka rincian.
- Target sentuh minimum, TalkBack label, dynamic font size, dark theme, dan kontras diuji pada Android.
