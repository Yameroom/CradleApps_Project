package com.example.cradleapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class TransactionActivity : AppCompatActivity() {

    // Pastikan IP ini tetap sesuai dengan IPv4 laptopmu
    private val ipLaptop = "10.64.137.120"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val etNomor = findViewById<EditText>(R.id.etNomorCradle)
        val etPengirim = findViewById<EditText>(R.id.etPengirim)
        val etTujuan = findViewById<EditText>(R.id.etTujuan)

        btnSimpan.setOnClickListener {
            val nomor = etNomor.text.toString().trim()
            val pengirim = etPengirim.text.toString().trim()
            val tujuan = etTujuan.text.toString().trim()

            if (nomor.isNotEmpty() && pengirim.isNotEmpty() && tujuan.isNotEmpty()) {
                simpanKeDatabase(nomor, pengirim, tujuan)
            } else {
                Toast.makeText(this, "Harap lengkapi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simpanKeDatabase(nomor: String, pengirim: String, tujuan: String) {
        val client = OkHttpClient()

        // Sesuaikan key (nomor_cradle, dll) dengan yang ada di $_POST file PHP kamu
        val formBody = FormBody.Builder()
            .add("nomor_cradle", nomor)
            .add("nama_pengirim", pengirim)
            .add("tujuan", tujuan)
            .build()

        val request = Request.Builder()
            .url("http://$ipLaptop/cradle_api/simpan_transaksi.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TransactionActivity, "Koneksi Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Gunakan .use {} agar response body otomatis tertutup setelah dibaca (mencegah memory leak)
                response.body()?.use { body ->
                    val responseData = body.string()

                    runOnUiThread {
                        if (response.isSuccessful && responseData.contains("success")) {
                            Toast.makeText(this@TransactionActivity, "Berhasil Simpan ke SSMS!", Toast.LENGTH_SHORT).show()

                            // Membersihkan form setelah berhasil
                            findViewById<EditText>(R.id.etNomorCradle).text.clear()
                            findViewById<EditText>(R.id.etPengirim).text.clear()
                            findViewById<EditText>(R.id.etTujuan).text.clear()
                        } else {
                            Toast.makeText(this@TransactionActivity, "Gagal Simpan: $responseData", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }
}