package com.example.cradleapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.text.NumberFormat
import java.util.*

class DetailReportActivity : AppCompatActivity() {

    private val urlServer = "https://estimable-subfulgently-margarete.ngrok-free.dev"
    private var idTrx: String? = null

    private var currentPKirim: Double = 0.0
    private var currentPAmbil: Double = 0.0
    private var currentCustomer: String = ""
    private var currentCradle: String = ""
    private var currentTanggal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_report)

        idTrx = intent.getStringExtra("ID_TRANSAKSI")

        findViewById<ImageButton>(R.id.btnBackDetail).setOnClickListener { finish() }

        displayDataFromIntent()

        findViewById<MaterialButton>(R.id.btnEditAction).setOnClickListener {
            val intentEdit = Intent(this, EditActivity::class.java)
            intentEdit.putExtra("ID_TRANSAKSI", idTrx)
            intentEdit.putExtra("CUSTOMER_NAME", currentCustomer)
            intentEdit.putExtra("CRADLE_NAME", currentCradle)
            intentEdit.putExtra("TANGGAL_KIRIM", currentTanggal)
            intentEdit.putExtra("TANGGAL_AMBIL", currentTanggal)
            intentEdit.putExtra("P_KIRIM", currentPKirim)
            intentEdit.putExtra("P_AMBIL", currentPAmbil)
            startActivity(intentEdit)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!idTrx.isNullOrEmpty()) {
            refreshDataFromServer(idTrx!!)
        }
    }

    private fun refreshDataFromServer(id: String) {
        val client = OkHttpClient()
        val url = "$urlServer/cradle_api/get_report.php?id=$id"

        val request = Request.Builder()
            .url(url)
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SERVER_ERROR", "Server failed to load: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
                        if (jsonObject.get("status").asString == "success") {
                            val dataArray = jsonObject.getAsJsonArray("data")
                            if (dataArray.size() > 0) {
                                val data = dataArray[0].asJsonObject
                                runOnUiThread { updateUI(data) }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PARSING_ERROR", "JSON parsing failed: ${e.message}")
                    }
                }
            }
        })
    }

    private fun updateUI(data: JsonObject) {
        try {
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)

            // --- HELPER UNTUK MENANGANI ANGKA .11 MENJADI 0.11 DAN STRING "32.0" ---
            fun getSafeDbl(key: String): Double {
                if (!data.has(key) || data.get(key).isJsonNull) return 0.0
                val valueStr = data.get(key).asString
                return try {
                    // Menambal angka yang diawali titik agar bisa diproses asDouble
                    val cleanedValue = if (valueStr.startsWith(".")) "0$valueStr" else valueStr
                    cleanedValue.toDouble()
                } catch (e: Exception) { 0.0 }
            }

            fun getSafeStr(key: String): String = if (data.has(key) && !data.get(key).isJsonNull) data.get(key).asString else "-"

            // 1. Update variabel lokal
            currentCustomer = getSafeStr("Customer")
            currentCradle = getSafeStr("Cradle")
            currentTanggal = getSafeStr("Tanggal")
            currentPKirim = getSafeDbl("P_Kirim")
            currentPAmbil = getSafeDbl("P_Ambil")

            // 2. Update Header
            findViewById<TextView>(R.id.tvDetailCradleName).text = currentCradle
            findViewById<TextView>(R.id.tvDetailCustomer).text = currentCustomer
            findViewById<TextView>(R.id.tvResTanggal).text = currentTanggal

            // 3. Update Tekanan
            findViewById<TextView>(R.id.tvResPKirim).text = currentPKirim.toInt().toString()
            findViewById<TextView>(R.id.tvResPAmbil).text = currentPAmbil.toInt().toString()

            // 4. Update Volume & Revenue (Menangani format .11)
            findViewById<TextView>(R.id.tvResSM3Kirim).text = String.format("%.2f M³", getSafeDbl("SM3_Kirim"))
            findViewById<TextView>(R.id.tvResSM3Ambil).text = String.format("%.2f M³", getSafeDbl("SM3_Ambil"))
            findViewById<TextView>(R.id.tvResSM3Total).text = String.format("%.2f M³", getSafeDbl("Nilai_SM3_Total"))
            findViewById<TextView>(R.id.tvResRevenue).text = formatRupiah.format(getSafeDbl("Revenue_IDR"))
            findViewById<TextView>(R.id.tvResHarga).text = formatRupiah.format(getSafeDbl("Harga"))

            // 5. Update AGA8 Chips
            findViewById<TextView>(R.id.chipCO2).text = String.format("CO2: %.2f%%", getSafeDbl("CO2"))
            findViewById<TextView>(R.id.chipN2).text = String.format("N2: %.2f%%", getSafeDbl("N2"))
            findViewById<TextView>(R.id.chipSG).text = String.format("SG: %.4f", getSafeDbl("SG"))
            findViewById<TextView>(R.id.chipFpvTotal).text = String.format("Fpv Total: %.4f", getSafeDbl("Fpv_Total"))

            // 6. Update Detail Teknis (Menangani string "32.0" ke Int)
            findViewById<TextView>(R.id.tvResTAwal).text = "Temp: ${getSafeDbl("T_Awal").toInt()}°C"
            findViewById<TextView>(R.id.tvResTAkhir).text = "Temp: ${getSafeDbl("T_Akhir").toInt()}°C"
            findViewById<TextView>(R.id.tvResFpvKirim).text = String.format("Fpv: %.4f", getSafeDbl("Fpv_Kirim"))
            findViewById<TextView>(R.id.tvResFpvAmbil).text = String.format("Fpv: %.4f", getSafeDbl("Fpv_Ambil"))

        } catch (e: Exception) {
            Log.e("UPDATE_UI_ERROR", "Error: ${e.message}")
        }
    }

    private fun displayDataFromIntent() {
        currentCustomer = intent.getStringExtra("CUSTOMER_NAME") ?: "-"
        currentCradle = intent.getStringExtra("CRADLE_NAME") ?: "-"
        currentTanggal = intent.getStringExtra("TANGGAL") ?: "-"
        currentPKirim = intent.getDoubleExtra("P_KIRIM", 0.0)
        currentPAmbil = intent.getDoubleExtra("P_AMBIL", 0.0)

        findViewById<TextView>(R.id.tvDetailCradleName).text = currentCradle
        findViewById<TextView>(R.id.tvDetailCustomer).text = currentCustomer
        findViewById<TextView>(R.id.tvResTanggal).text = currentTanggal
        findViewById<TextView>(R.id.tvResPKirim).text = currentPKirim.toInt().toString()
        findViewById<TextView>(R.id.tvResPAmbil).text = currentPAmbil.toInt().toString()
        findViewById<TextView>(R.id.tvResSM3Total).text = String.format("%.2f M³", intent.getDoubleExtra("SM3_TOTAL", 0.0))
    }
}