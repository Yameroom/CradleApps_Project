package com.example.cradleapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.*

class DetailReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_report)

        // 1. Inisialisasi Tombol Back
        val btnBack = findViewById<ImageButton>(R.id.btnBackDetail)
        btnBack.setOnClickListener { finish() }

        // 2. Ambil data dari Intent (Pastikan KEY sama dengan yang di kirim dari ReportAdapter)
        val cradleName = intent.getStringExtra("CRADLE_NAME") ?: "-"
        val customer = intent.getStringExtra("CUSTOMER_NAME") ?: "-"
        val tanggal = intent.getStringExtra("TANGGAL") ?: "-"
        val revenue = intent.getDoubleExtra("REVENUE", 0.0)

        // Data Teknis Utama
        val pKirim = intent.getDoubleExtra("P_KIRIM", 0.0)
        val pAmbil = intent.getDoubleExtra("P_AMBIL", 0.0)
        val sm3Kirim = intent.getDoubleExtra("SM3_KIRIM", 0.0)
        val sm3Ambil = intent.getDoubleExtra("SM3_AMBIL", 0.0)
        val fpvTotal = intent.getDoubleExtra("FPV_TOTAL", 0.0)
        val sm3Total = intent.getDoubleExtra("SM3_TOTAL", 0.0)

        // Parameter AGA8 Tambahan dari Stored Procedure
        val tAwal = intent.getDoubleExtra("T_AWAL", 32.0)
        val tAkhir = intent.getDoubleExtra("T_AKHIR", 27.0)
        val co2 = intent.getDoubleExtra("CO2", 0.8477)
        val n2 = intent.getDoubleExtra("N2", 0.7632)
        val sg = intent.getDoubleExtra("SG", 0.5841)
        val harga = intent.getDoubleExtra("HARGA", 0.0)

        // 3. Pasang data ke UI Header
        findViewById<TextView>(R.id.tvDetailCradleName).text = cradleName

        // Format Revenue ke Rupiah
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        findViewById<TextView>(R.id.tvResRevenue).text = formatRupiah.format(revenue)

        // 4. Pasang ke Grid menggunakan Fungsi Helper (Total 14 Parameter)

        // Baris 1: Pressure
        setupGridValue(R.id.gridPKirim, "Pressure Kirim", "$pKirim Bar")
        setupGridValue(R.id.gridPAmbil, "Pressure Ambil", "$pAmbil Bar")

        // Baris 2: SM3 Individu
        setupGridValue(R.id.gridSM3Kirim, "SM3 Kirim", String.format("%.2f m3", sm3Kirim))
        setupGridValue(R.id.gridSM3Ambil, "SM3 Ambil", String.format("%.2f m3", sm3Ambil))

        // Baris 3: Fpv & Total SM3
        setupGridValue(R.id.gridFpvTotal, "Fpv Total", String.format("%.4f", fpvTotal))
        setupGridValue(R.id.gridSM3Total, "Net SM3 Total", String.format("%.2f m3", sm3Total))

        // Baris 4: Suhu (T Awal & T Akhir)
        setupGridValue(R.id.gridTAwal, "Temp Awal", "$tAwal °C")
        setupGridValue(R.id.gridTAkhir, "Temp Akhir", "$tAkhir °C")

        // Baris 5: Gas Content (CO2 & N2)
        setupGridValue(R.id.gridCO2, "CO2 Content", "$co2 %")
        setupGridValue(R.id.gridN2, "N2 Content", "$n2 %")

        // Baris 6: SG & Harga
        setupGridValue(R.id.gridSG, "Spec. Gravity", "$sg")
        setupGridValue(R.id.gridHarga, "Harga/m3", formatRupiah.format(harga))

        // Baris 7: Info Customer & Waktu
        setupGridValue(R.id.gridCustomer, "Customer", customer)
        setupGridValue(R.id.gridTanggal, "Tanggal Ambil", tanggal)
    }

    private fun setupGridValue(viewId: Int, label: String, value: String) {
        val view = findViewById<View>(viewId)
        // Jika baris bawah ini merah, pastikan di item_detail_grid.xml ID-nya benar
        // Gunakan ID tvGridLabel dan tvGridValue sesuai kode Abang sebelumnya
        view.findViewById<TextView>(R.id.tvGridLabel).text = label
        view.findViewById<TextView>(R.id.tvGridValue).text = value
    }
}