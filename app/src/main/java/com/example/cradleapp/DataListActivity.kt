package com.example.cradleapp

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class DataListActivity : AppCompatActivity() {

    private lateinit var rvTransaksi: RecyclerView
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabExportExcel: FloatingActionButton

    // API Link to ngrok domain
    private val urlServer = "https://estimable-subfulgently-margarete.ngrok-free.dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)

        // Inisialisasi View
        rvTransaksi = findViewById(R.id.rvTransaksi)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        fabExportExcel = findViewById(R.id.fabExportExcel)

        rvTransaksi.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }

        // Fitur 1: Pull to Refresh
        swipeRefresh.setOnRefreshListener {
            ambilDataLengkap("", "", "")
        }

        // Fitur 2: Export Excel Sultan (Format CSV Anti-Corrupt)
        fabExportExcel.setOnClickListener {
            unduhExcelLaporan()
        }
    }

    override fun onResume() {
        super.onResume()
        ambilDataLengkap("", "", "")
    }

    private fun unduhExcelLaporan() {
        try {
            // URL ke file PHP pembuat CSV
            val downloadUrl = "$urlServer/cradle_api/export_excel.php"

            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Cradle Report")
                .setDescription("Downloading format CSV...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // Ganti ekstensi ke .csv agar Excel tidak protes korup
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Laporan_Cradle_${System.currentTimeMillis()}.csv")
                .addRequestHeader("ngrok-skip-browser-warning", "true")

            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

            Toast.makeText(this, "Downloading report now, mate!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed ?: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ambilDataLengkap(customer: String, year: String, month: String) {
        swipeRefresh.isRefreshing = true

        val client = OkHttpClient()
        val url = "$urlServer/cradle_api/get_report.php?customer=$customer&year=$year&month=$month"

        val request = Request.Builder()
            .url(url)
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(this@DataListActivity, "Server offline Bang", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        // Perbaikan: Tambahkan penentuan tipe <JsonObject> secara eksplisit
                        val jsonObject = Gson().fromJson<JsonObject>(json, JsonObject::class.java)
                        val status = jsonObject.get("status").asString

                        if (status == "success") {
                            val dataArray = jsonObject.getAsJsonArray("data")
                            val type = object : TypeToken<List<ReportModel>>() {}.type
                            val dataList: List<ReportModel> = Gson().fromJson(dataArray, type)

                            runOnUiThread {
                                swipeRefresh.isRefreshing = false
                                reportAdapter = ReportAdapter(this@DataListActivity, dataList)
                                rvTransaksi.adapter = reportAdapter
                                rvTransaksi.scrollToPosition(0)
                            }
                        } else {
                            runOnUiThread {
                                swipeRefresh.isRefreshing = false
                                rvTransaksi.adapter = ReportAdapter(this@DataListActivity, emptyList())
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread { swipeRefresh.isRefreshing = false }
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    fun konfirmasiHapus(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Data Deletion")
            .setMessage("Are you sure you want to delete this data, buddy? Make sure you don't cry after this.")
            .setPositiveButton("Yes :V") { _, _ ->
                prosesHapusKeServer(id)
            }
            .setNegativeButton("No :)", null)
            .show()
    }

    private fun prosesHapusKeServer(id: String) {
        val client = OkHttpClient()
        val formBody = FormBody.Builder().add("id", id).build()

        val request = Request.Builder()
            .url("$urlServer/cradle_api/hapus_data.php")
            .post(formBody)
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DataListActivity, "Server trouble", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@DataListActivity, "Data deleted!", Toast.LENGTH_SHORT).show()
                    ambilDataLengkap("", "", "")
                }
            }
        })
    }
}