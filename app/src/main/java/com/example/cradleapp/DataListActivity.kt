package com.example.cradleapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class DataListActivity : AppCompatActivity() {

    private lateinit var rvTransaksi: RecyclerView
    private lateinit var reportAdapter: ReportAdapter
    private val ipLaptop = "192.168.0.116"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)

        rvTransaksi = findViewById(R.id.rvTransaksi)
        rvTransaksi.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }

        // Load data pertama kali
        ambilDataLengkap("", "", "")
    }

    private fun ambilDataLengkap(customer: String, year: String, month: String) {
        val client = OkHttpClient()

        // Memastikan parameter dikirim sebagai string kosong agar SP SQL mengerti itu adalah 'Tampilkan Semua'
        val url = "http://$ipLaptop/cradle_api/get_report.php?customer=$customer&year=$year&month=$month"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DataListActivity, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
                        val status = jsonObject.get("status").asString

                        if (status == "success") {
                            val dataArray = jsonObject.getAsJsonArray("data")
                            val type = object : TypeToken<List<ReportModel>>() {}.type
                            val dataList: List<ReportModel> = Gson().fromJson(dataArray, type)

                            runOnUiThread {
                                reportAdapter = ReportAdapter(this@DataListActivity, dataList)
                                rvTransaksi.adapter = reportAdapter
                            }
                        } else {
                            val message = jsonObject.get("message").asString
                            runOnUiThread {
                                rvTransaksi.adapter = ReportAdapter(this@DataListActivity, emptyList())
                                Toast.makeText(this@DataListActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    // Fungsi konfirmasi hapus dipanggil dari Adapter
    fun konfirmasiHapus(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus data transaksi ini secara permanen?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                prosesHapusKeServer(id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun prosesHapusKeServer(id: String) {
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("id", id) // "id" harus sama dengan $_POST['id'] di PHP
            .build()

        val request = Request.Builder()
            .url("http://$ipLaptop/cradle_api/hapus_data.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DataListActivity, "Gagal koneksi server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val responseData = body.string()
                    runOnUiThread {
                        if (responseData.contains("success")) {
                            Toast.makeText(this@DataListActivity, "Data Berhasil Terhapus", Toast.LENGTH_SHORT).show()
                            // Segarkan list agar data yang dihapus hilang
                            ambilDataLengkap("", "", "")
                        } else {
                            Toast.makeText(this@DataListActivity, "Gagal hapus: $responseData", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }
}