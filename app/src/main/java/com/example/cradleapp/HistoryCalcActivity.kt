package com.example.cradleapp

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class HistoryCalcActivity : AppCompatActivity() {
    private lateinit var rvHistory: RecyclerView
    private val client = OkHttpClient()

    // Ngrok API Domain Server
    private val urlServer = "https://estimable-subfulgently-margarete.ngrok-free.dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_calc)

        rvHistory = findViewById(R.id.rvHistoryCalc)
        rvHistory.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBackHistory).setOnClickListener { finish() }

        loadHistory()
    }

    private fun loadHistory() {
        val username = SessionManager(this).getUsername() ?: ""
        val url = "$urlServer/cradle_api/get_history_calc.php?username=$username"

        // Header 'ngrok-skip-browser-warning'
        val request = Request.Builder()
            .url(url)
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@HistoryCalcActivity, "Server is like italian snap their pasta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        val responseObj = Gson().fromJson(json, com.google.gson.JsonObject::class.java)

                        if (responseObj.get("status").asString == "success") {
                            val type = object : TypeToken<List<HistoryCalc>>() {}.type
                            val list: List<HistoryCalc> = Gson().fromJson(responseObj.get("data"), type)

                            runOnUiThread {
                                rvHistory.adapter = HistoryCalcAdapter(list)
                            }
                        } else {
                            val msg = if (responseObj.has("message")) responseObj.get("message").asString else "Start Calculate mate to see your history"
                            runOnUiThread {
                                Toast.makeText(this@HistoryCalcActivity, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HISTORY_ERROR", "Parsing error: ${e.message}")
                    }
                }
            }
        })
    }
}