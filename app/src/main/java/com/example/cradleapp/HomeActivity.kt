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
    private lateinit var tvHelloUser: TextView // Tambahan untuk Nama User
    private val ipLaptop = "192.168.0.116"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Inisialisasi View Statistik & Welcome Message
        tvTotalData = findViewById(R.id.tvTotalData)
        tvTodayData = findViewById(R.id.tvTodayData)
        tvHelloUser = findViewById(R.id.tvHelloUser) // ID yang baru kita tambah di XML

        // 2. Ambil Nama User dari Intent Login
        // Menggunakan key "USER_NAME", pastikan di MainActivity pengirimannya sama
        val userName = intent.getStringExtra("USER_NAME") ?: "Admin"
        tvHelloUser.text = "Hello $userName,"

        // 3. Inisialisasi Menu Navigasi
        val cardInput = findViewById<CardView>(R.id.cardTransaksi)
        val cardList = findViewById<CardView>(R.id.cardViewData)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)

        // Klik untuk ke halaman Input
        cardInput.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

        // Klik untuk ke halaman List
        cardList.setOnClickListener {
            val intent = Intent(this, DataListActivity::class.java)
            startActivity(intent)
        }

        // Logika Logout
        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

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