package com.example.cradleapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // IP tetap gunakan yang sudah kamu catat (pastikan satu WiFi)
    private val ipLaptop = "10.64.137.120"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi username dan password!", Toast.LENGTH_SHORT).show()
            } else {
                prosesLogin(user, pass)
            }
        }
    }

    private fun prosesLogin(user: String, pass: String) {
        val client = OkHttpClient()

        // Membuat data form POST
        val formBody = FormBody.Builder()
            .add("username", user)
            .add("password", pass)
            .build()

        val request = Request.Builder()
            .url("http://$ipLaptop/cradle_api/login.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Gagal konek: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Menggunakan .use agar response body otomatis ditutup
                response.body()?.use { body ->
                    val responseData = body.string()

                    runOnUiThread {
                        if (response.isSuccessful && responseData.contains("success")) {
                            Toast.makeText(this@MainActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                            // PERBAIKAN: Baris di bawah ini sekarang sudah AKTIF (tidak di-comment lagi)
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()

                            // Tutup activity login agar tidak bisa "back" lagi ke sini
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, "Username atau Password Salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}