package com.example.cradleapp

// Ini adalah model data yang cocok dengan kolom di database SSMS kamu
data class Transaksi(
    val id: String,
    val nomor_cradle: String,
    val nama_pengirim: String,
    val tujuan: String,
    val tanggal_kirim: String
)