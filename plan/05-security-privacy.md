# Security & Privacy Plan

## Data classification

Data dompet, nominal, merchant, note, kontak, foto struk, serta backup adalah data finansial/pribadi sensitif. Data ini tidak boleh masuk analytics, log, screenshot otomatis, nama work request, atau navigation route.

## Kontrol wajib

| Area | Kontrol |
|---|---|
| Database | SQLCipher; passphrase tidak ditulis ke disk/log dan key material dibungkus Android Keystore. |
| PIN | Simpan verifier salted dan parameter KDF, bukan PIN plaintext. Terapkan throttling setelah kegagalan berulang. |
| Biometrik | Gunakan BiometricPrompt sebagai pembuka key/app-lock; PIN fallback sesuai pilihan pengguna. |
| App lock | Lock pada launch dan setelah timeout background; clear sensitive in-memory form state saat terkunci. |
| Tampilan | Opsi `FLAG_SECURE` untuk recent-app/screenshot; jangan membuatnya satu-satunya kontrol keamanan. |
| Backup | Enkripsi terpisah dari enkripsi database; password backup tidak dapat dipulihkan dan tidak disimpan. |
| Log | Debug log finansial dilarang; release logging memakai event/error code tersanitasi. |
| Permissions | Minta kamera, notifikasi, media, atau exact alarm hanya pada aksi yang membutuhkan; ada fallback bila ditolak. |
| Intent | Component exported hanya bila diperlukan; validasi data intent/deep link dan gunakan PendingIntent immutable kecuali harus mutable. |

## Threat model minimum

- Perangkat dipinjam saat aplikasi ada di recent apps.
- Database/file backup dicopy dari storage atau perangkat hilang.
- Intent/deep-link berbahaya mencoba membuka atau mengubah data.
- Crash/log/analytics membocorkan isi transaksi.
- Restore backup rusak atau dari versi schema tak kompatibel.

## Kebijakan recovery

Lupa PIN tidak boleh membuka data lokal. Recovery adalah reset aplikasi lalu restore dari backup yang dapat didekripsi pengguna. Copy ini harus ditampilkan sebelum PIN diaktifkan. Kegagalan backup/restore tidak boleh menghapus data lokal yang ada.

## Review gate

Setiap dependency keamanan, permission baru, export component, format backup, dan akses jaringan harus direview sebagai perubahan security dan dicatat di ADR bila memengaruhi desain jangka panjang.
