package com.example.cradleapp

data class Transaksi(
    val id: String,
    val nomor_cradle: String,
    val nama_pengirim: String,
    val tujuan: String,
    val tanggal_kirim: String
)