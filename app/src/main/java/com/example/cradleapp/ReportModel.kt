package com.example.cradleapp
// Sesuaikan dengan package project

data class ReportModel(
    val id_transaksi: String? = null,
    val Customer: String,
    val Year: String,
    val Month: String,
    val Period: String,
    val Tanggal: String,
    val Cradle: String,
    val P_Kirim: String,
    val P_Ambil: String,
    val CO2: Double,
    val N2: Double,
    val SG: Double,
    val T_Awal: Double,
    val T_Akhir: Double,
    val Fpv_Kirim: Double,
    val Fpv_Ambil: Double,
    val Fpv_Total: Double,
    val SM3_Kirim: Double,
    val SM3_Ambil: Double,
    val Nilai_SM3_Total: Double,
    val Harga: Double,
    val Revenue_IDR: Double
)