package com.example.cradleapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {


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

        // Kirim data login via POST
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
                    Toast.makeText(this@MainActivity, "Gagal konek server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val responseData = body.string()

                    runOnUiThread {
                        try {
                            // 1. Parse JSON dari PHP menggunakan Gson
                            val jsonObject = Gson().fromJson(responseData, JsonObject::class.java)
                            val status = jsonObject.get("status").asString

                            if (status == "success") {
                                // 2. Ambil "nama_lengkap" dari database melalui JSON
                                val namaAsli = if (jsonObject.has("nama_lengkap")) {
                                    jsonObject.get("nama_lengkap").asString
                                } else {
                                    "Admin"
                                }

                                Toast.makeText(this@MainActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                                // 3. Kirim NAMA ASLI ke HomeActivity
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.putExtra("USER_NAME", namaAsli)
                                startActivity(intent)

                                // Tutup halaman login
                                finish()
                            } else {
                                val message = jsonObject.get("message").asString
                                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Error data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}