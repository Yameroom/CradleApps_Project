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

    // API Ngrok Server Domain
    private val urlServer = "https://estimable-subfulgently-margarete.ngrok-free.dev"
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Session user checker
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Yeah who need password anyways :)", Toast.LENGTH_SHORT).show()
            } else {
                prosesLogin(user, pass)
            }
        }
    }

    private fun prosesLogin(user: String, pass: String) {
        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("username", user)
            .add("password", pass)
            .build()

        val request = Request.Builder()
            .url("$urlServer/cradle_api/login.php")
            .post(formBody)
            .addHeader("ngrok-skip-browser-warning", "true") // <--- Bypass Ngrok Interstitial
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed Connection to Server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val responseData = body.string()

                    runOnUiThread {
                        try {
                            val jsonObject = Gson().fromJson(responseData, JsonObject::class.java)
                            val status = jsonObject.get("status").asString

                            if (status == "success") {
                                val dataObj = jsonObject.getAsJsonObject("data")

                                val namaAsli = if (dataObj.has("nama")) {
                                    dataObj.get("nama").asString
                                } else {
                                    user
                                }

                                // Simpan ke SessionManager
                                sessionManager.saveLoginStatus(true, namaAsli)

                                Toast.makeText(this@MainActivity, "Welcome Back, $namaAsli", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val message = jsonObject.get("message").asString
                                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {

                            Toast.makeText(this@MainActivity, "Error Response: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}