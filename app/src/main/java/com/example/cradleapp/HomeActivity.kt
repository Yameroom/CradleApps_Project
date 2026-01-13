package com.example.cradleapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var tvTotalData: TextView
    private lateinit var tvTodayData: TextView
    private lateinit var tvHelloUser: TextView

    // IP Laptop terbaru sesuai input Abang
    private val ipLaptop = "10.64.137.120"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Inisialisasi View Statistik & Welcome Message
        tvTotalData = findViewById(R.id.tvTotalData)
        tvTodayData = findViewById(R.id.tvTodayData)
        tvHelloUser = findViewById(R.id.tvHelloUser)

        // 2. Ambil Nama User dari Intent Login
        val userName = intent.getStringExtra("USER_NAME") ?: "Admin"
        tvHelloUser.text = "Hello $userName,"

        // 3. Inisialisasi Menu Navigasi
        val cardInput = findViewById<CardView>(R.id.cardTransaksi)
        val cardList = findViewById<CardView>(R.id.cardViewData)
        val cardCalculator = findViewById<CardView>(R.id.cardCalculator) // Inisialisasi Menu Kalkulator
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)

        // Klik untuk ke halaman Input Transaksi
        cardInput.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

        // Klik untuk ke halaman List Data
        cardList.setOnClickListener {
            val intent = Intent(this, DataListActivity::class.java)
            startActivity(intent)
        }

        // Klik untuk ke halaman Kalkulator AGA8 (Menu Baru)
        cardCalculator.setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            // Kirim userName agar history kalkulator tercatat atas nama user tersebut
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        // Logika Logout dengan Konfirmasi
        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    // Clear task agar user tidak bisa tekan tombol back kembali ke Home
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    // Refresh statistik setiap kali user kembali ke halaman Home
    override fun onResume() {
        super.onResume()
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        val client = OkHttpClient()
        val url = "http://$ipLaptop/cradle_api/get_stats.php"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Jika koneksi gagal (misal server mati)
                    tvTotalData.text = "-"
                    tvTodayData.text = "-0"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
                        if (jsonObject.get("status").asString == "success") {
                            val total = jsonObject.get("total").asString
                            val today = jsonObject.get("today").asString

                            runOnUiThread {
                                // Update UI Dashboard
                                tvTotalData.text = total
                                tvTodayData.text = "+$today"
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }
}