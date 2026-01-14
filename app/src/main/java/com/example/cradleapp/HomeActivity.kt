package com.example.cradleapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var tvTotalData: TextView
    private lateinit var tvTodayData: TextView
    private lateinit var tvHelloUser: TextView
    private lateinit var tvAvgPressure: TextView
    private lateinit var tvAvgSM3: TextView
    private lateinit var tvAvgRevenue: TextView

    private lateinit var session: SessionManager
    private val urlServer = "https://estimable-subfulgently-margarete.ngrok-free.dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        session = SessionManager(this)

        tvTotalData = findViewById(R.id.tvTotalData)
        tvTodayData = findViewById(R.id.tvTodayData)
        tvHelloUser = findViewById(R.id.tvHelloUser)
        tvAvgPressure = findViewById(R.id.tvAvgPressure)
        tvAvgSM3 = findViewById(R.id.tvAvgSM3)
        tvAvgRevenue = findViewById(R.id.tvAvgRevenue)

        val userName = session.getUsername() ?: "Admin"
        tvHelloUser.text = "Hello $userName,"

        // Navigation
        findViewById<CardView>(R.id.cardTransaksi).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        findViewById<CardView>(R.id.cardViewData).setOnClickListener {
            startActivity(Intent(this, DataListActivity::class.java))
        }

        findViewById<CardView>(R.id.cardCalculator).setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Nooo, Please don't do this to me mate. We are friend right ? :(")
                .setPositiveButton("Yes") { _, _ ->
                    session.logout()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No :)", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$urlServer/cradle_api/get_stats.php")
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    tvTotalData.text = "-"
                    tvTodayData.text = "0"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
                        if (jsonObject.has("status") && jsonObject.get("status").asString == "success") {
                            val data = jsonObject.getAsJsonObject("data")

                            runOnUiThread {
                                // Total Data & Today
                                tvTotalData.text = if (data.has("total")) data.get("total").asString else "0"
                                tvTodayData.text = if (data.has("today")) "+${data.get("today").asString}" else "+0"

                                // Average Stats dengan penanganan Double agar tidak error format ilmiah
                                val press = getSafeDbl(data, "avg_pressure")
                                val sm3 = getSafeDbl(data, "avg_sm3")
                                val revenue = getSafeDbl(data, "avg_revenue")

                                tvAvgPressure.text = String.format("%.1f Bar", press)
                                tvAvgSM3.text = String.format("%.1f M³", sm3)

                                // Gunakan penyingkat angka otomatis (Rp ... Jt / M)
                                tvAvgRevenue.text = formatSultanCurrency(revenue)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("JSON_ERROR", "Refresh Failed: ${e.message}")
                    }
                }
            }
        })
    }

    // Fungsi Helper untuk ambil Double dengan aman
    private fun getSafeDbl(data: JsonObject, key: String): Double {
        return try {
            if (data.has(key) && !data.get(key).isJsonNull) {
                val valueStr = data.get(key).asString
                // Tangani jika ada angka malas seperti .11
                val cleaned = if (valueStr.startsWith(".")) "0$valueStr" else valueStr
                cleaned.toDouble()
            } else 0.0
        } catch (e: Exception) { 0.0 }
    }

    // Fungsi Sakti Penyingkat Angka agar Layout tidak hancur
    private fun formatSultanCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> String.format("Rp %.2f M", amount / 1_000_000_000)
            amount >= 1_000_000 -> String.format("Rp %.2f Jt", amount / 1_000_000)
            amount >= 1_000 -> String.format("Rp %.1f Rb", amount / 1_000)
            else -> {
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                formatRupiah.format(amount).replace("Rp", "Rp ")
            }
        }
    }
}