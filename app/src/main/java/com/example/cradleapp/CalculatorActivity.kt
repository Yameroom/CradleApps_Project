package com.example.cradleapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.util.Locale

class CalculatorActivity : AppCompatActivity() {

    private lateinit var etTemp: EditText
    private lateinit var etPress: EditText
    private lateinit var etLwc: EditText
    private lateinit var etSG: EditText
    private lateinit var etCO2: EditText
    private lateinit var etN2: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnHistory: Button
    private lateinit var tvResultSM3: TextView
    private lateinit var btnBack: ImageButton

    // API Menggunakan Ngrok Cihuy
    private val urlServer = "https://estimable-subfulgently-margarete.ngrok-free.dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        // Inisialisasi View page
        etTemp = findViewById(R.id.etCalcTemp)
        etPress = findViewById(R.id.etCalcPress)
        etLwc = findViewById(R.id.etCalcLwc)
        etSG = findViewById(R.id.etCalcSG)
        etCO2 = findViewById(R.id.etCalcCO2)
        etN2 = findViewById(R.id.etCalcN2)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnHistory = findViewById(R.id.btnHistory)
        tvResultSM3 = findViewById(R.id.tvResultSM3)
        btnBack = findViewById(R.id.btnBackCalc)

        btnBack.setOnClickListener { finish() }

        btnCalculate.setOnClickListener { prosesHitung() }

        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryCalcActivity::class.java)
            startActivity(intent)
        }
    }

    private fun prosesHitung() {
        val session = SessionManager(this)
        val username = session.getUsername() ?: "Admin"

        val temp = etTemp.text.toString().trim().replace(",", ".")
        val pressure = etPress.text.toString().trim().replace(",", ".")
        val lwc = etLwc.text.toString().trim().replace(",", ".")
        val sg = etSG.text.toString().trim().replace(",", ".")
        val co2 = etCO2.text.toString().trim().replace(",", ".")
        val n2 = etN2.text.toString().trim().replace(",", ".")

        if (temp.isEmpty() || pressure.isEmpty() || lwc.isEmpty()) {
            Toast.makeText(this, "Fill it, or I will make you bald", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("temp", temp)
            .add("pressure", pressure)
            .add("lwc", lwc)
            .add("sg", sg)
            .add("co2", co2)
            .add("n2", n2)
            .build()

        //URL Ngrok dan Header Skip Warning
        val request = Request.Builder()
            .url("$urlServer/cradle_api/proses_kalkulator.php")
            .post(formBody)
            .addHeader("ngrok-skip-browser-warning", "true") // <--- Bypass Ngrok Warning
            .build()

        btnCalculate.isEnabled = false
        btnCalculate.text = "Calculating..."

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    btnCalculate.isEnabled = true
                    btnCalculate.text = "PROCESS"
                    Toast.makeText(this@CalculatorActivity, "Server is offline/trouble", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val responseData = body.string()
                    runOnUiThread {
                        btnCalculate.isEnabled = true
                        btnCalculate.text = "PROCESS"

                        try {
                            val jsonObject = Gson().fromJson(responseData, JsonObject::class.java)
                            if (jsonObject.get("status").asString == "success") {
                                val resultData = jsonObject.getAsJsonObject("data")

                                // Format hasil agar rapi 2 angka belakang koma
                                val hasilSm3Raw = resultData.get("hasil_sm3").asDouble
                                val hasilFormatted = String.format(Locale.US, "%.2f", hasilSm3Raw)

                                tvResultSM3.text = "$hasilFormatted M³"
                                Toast.makeText(this@CalculatorActivity, "We did it mate", Toast.LENGTH_SHORT).show()
                            } else {
                                val msg = if (jsonObject.has("message")) jsonObject.get("message").asString else "Calculation Failed dawg :( Try again later"
                                Toast.makeText(this@CalculatorActivity, msg, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@CalculatorActivity, "Error parsing data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}