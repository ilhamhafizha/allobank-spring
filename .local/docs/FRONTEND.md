# Frontend Split Bill

Frontend HTML, CSS, dan JavaScript disajikan langsung oleh Spring Boot. Tidak perlu Node.js, instalasi npm, build frontend, atau konfigurasi CORS.

## Menjalankan

```sh
docker compose up --build -d
```

Buka **http://localhost:4110/**. Untuk konfigurasi database dan menjalankan tanpa Docker, lihat README.md.

## Alur penggunaan

1. Klik **Buat grup**, isi nama grup dan nama anggota (satu nama per baris).
2. Pilih grup, klik **Pengeluaran**, masukkan deskripsi, nominal, pembayar, dan anggota yang ikut pembagian.
3. Buka **Settlement** untuk mengambil perhitungan terbaru dari server, termasuk biaya layanan dan transfer antaranggota.

Tampilan menyesuaikan layar desktop dan ponsel. Form memiliki validasi, status penyimpanan, pesan kegagalan, pilihan penerima pembagian, dan tombol mencoba ulang settlement. Dialog dapat ditutup dengan tombol tutup atau Escape.

## Penyimpanan dan batasan API

Grup dan pengeluaran dibuat melalui API; perhitungan settlement selalu diambil dari backend. Daftar grup dan pengeluaran yang pernah dibuat melalui browser ini disimpan dalam localStorage karena API belum menyediakan endpoint GET untuk daftar/detail grup maupun daftar pengeluaran. Browser/perangkat lain tidak otomatis menampilkan riwayat tersebut. Menghapus data browser menghilangkan riwayat frontend, tetapi tidak menghapus data server.

Nominal menggunakan satu mata uang, ditampilkan sebagai rupiah. Settlement tidak mencatat atau menjalankan pembayaran. Frontend tidak menampilkan status lunas karena backend belum menyediakan pencatatan pembayaran.

## Berkas

- `src/main/resources/static/index.html`: halaman dan dialog.
- `src/main/resources/static/styles.css`: tema dan layout responsif.
- `src/main/resources/static/app.js`: interaksi, penyimpanan riwayat browser, dan integrasi API.
