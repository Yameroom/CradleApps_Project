package com.example.cradleapp

data class HistoryCalc(
    val id_history: String,
    val pressure_bar: Double,
    val temp_c: Double,
    val tube_volume_liter: Double,
    val co2: Double,
    val n2: Double,
    val sg: Double,
    val fpv: Double,
    val fpv2: Double,
    val hasil_sm3: Double,
    val created_at: String
)