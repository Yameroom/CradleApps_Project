package com.example.cradleapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val id = intent.getStringExtra("ID")
        val etNomor = findViewById<EditText>(R.id.etEditNomor)
        val etPengirim = findViewById<EditText>(R.id.etEditPengirim)
        val etTujuan = findViewById<EditText>(R.id.etEditTujuan)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanEdit)

        // Set teks awal sesuai data yang dipilih
        etNomor.setText(intent.getStringExtra("NOMOR"))
        etPengirim.setText(intent.getStringExtra("PENGIRIM"))
        etTujuan.setText(intent.getStringExtra("TUJUAN"))

        btnSimpan.setOnClickListener {
            val formBody = FormBody.Builder()
                .add("id", id ?: "")
                .add("nomor_cradle", etNomor.text.toString())
                .add("nama_pengirim", etPengirim.text.toString())
                .add("tujuan", etTujuan.text.toString())
                .build()

            val request = Request.Builder()
                .url("http://10.64.137.120/cradle_api/update_data.php")
                .post(formBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        Toast.makeText(this@EditActivity, "Data Berhasil Diupdate", Toast.LENGTH_SHORT).show()
                        finish() // Kembali ke list
                    }
                }
            })
        }
    }
}