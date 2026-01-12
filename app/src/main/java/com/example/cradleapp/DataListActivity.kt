package com.example.cradleapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class DataListActivity : AppCompatActivity() {

    private lateinit var rvTransaksi: RecyclerView
    private val ipLaptop = "10.64.137.120" // GANTI DENGAN IP KAMU

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)

        rvTransaksi = findViewById(R.id.rvTransaksi)
        rvTransaksi.layoutManager = LinearLayoutManager(this)

        ambilDataDariServer()
    }

    private fun ambilDataDariServer() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ipLaptop/cradle_api/ambil_data.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DataListActivity, "Gagal ambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                if (json != null) {
                    val type = object : TypeToken<List<Transaksi>>() {}.type
                    val dataList: List<Transaksi> = Gson().fromJson(json, type)

                    runOnUiThread {
                        rvTransaksi.adapter = TransaksiAdapter(dataList)
                    }
                }
            }
        })
    }

    // --- TAMBAHKAN DUA FUNGSI DI BAWAH INI ---

    // 1. Fungsi untuk menampilkan Dialog Konfirmasi
    fun konfirmasiHapus(id: String, position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Hapus Data")
        builder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
        builder.setPositiveButton("Ya") { _, _ ->
            prosesHapusKeServer(id)
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    // 2. Fungsi untuk mengirim request hapus ke PHP
    private fun prosesHapusKeServer(id: String) {
        val formBody = FormBody.Builder()
            .add("id", id)
            .build()

        val request = Request.Builder()
            .url("http://$ipLaptop/cradle_api/hapus_data.php")
            .post(formBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DataListActivity, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@DataListActivity, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show()
                    // Memanggil kembali data agar list terupdate otomatis
                    ambilDataDariServer()
                }
            }
        })
    }
}