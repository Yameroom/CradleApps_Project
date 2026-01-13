package com.example.cradleapp

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
    private lateinit var tvResultSM3: TextView
    private lateinit var btnBack: ImageButton

    // Gunakan IP yang sama dengan HomeActivity
    private val ipLaptop = "10.64.137.120"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        // 1. Inisialisasi View
        etTemp = findViewById(R.id.etCalcTemp)
        etPress = findViewById(R.id.etCalcPress)
        etLwc = findViewById(R.id.etCalcLwc)
        etSG = findViewById(R.id.etCalcSG)
        etCO2 = findViewById(R.id.etCalcCO2)
        etN2 = findViewById(R.id.etCalcN2)
        btnCalculate = findViewById(R.id.btnCalculate)
        tvResultSM3 = findViewById(R.id.tvResultSM3)
        btnBack = findViewById(R.id.btnBackCalc)

        // 2. Klik Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // 3. Klik Tombol Hitung
        btnCalculate.setOnClickListener {
            prosesHitung()
        }
    }

    private fun prosesHitung() {
        val username = intent.getStringExtra("USER_NAME") ?: "Admin"
        val client = OkHttpClient()

        // Ambil data dari semua EditText
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("temp", etTemp.text.toString())
            .add("pressure", etPress.text.toString())
            .add("lwc", etLwc.text.toString())
            .add("sg", etSG.text.toString())
            .add("co2", etCO2.text.toString())
            .add("n2", etN2.text.toString())
            .build()

        val request = Request.Builder()
            .url("http://$ipLaptop/cradle_api/proses_kalkulator.php")
            .post(formBody)
            .build()

        btnCalculate.isEnabled = false
        btnCalculate.text = "Calculating..."

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    btnCalculate.isEnabled = true
                    btnCalculate.text = "PROCESS CALCULATION"
                    Toast.makeText(this@CalculatorActivity, "Gagal konek server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val responseData = body.string()
                    runOnUiThread {
                        btnCalculate.isEnabled = true
                        btnCalculate.text = "PROCESS CALCULATION"

                        try {
                            val jsonObject = Gson().fromJson(responseData, JsonObject::class.java)
                            if (jsonObject.get("status").asString == "success") {
                                val resultData = jsonObject.getAsJsonObject("data")

                                // PERBAIKAN: Ambil sebagai Double untuk formatting
                                val hasilSm3Raw = resultData.get("hasil_sm3").asDouble

                                // Format agar muncul 0.22 (memaksa leading zero dan 2 desimal)
                                val hasilFormatted = String.format(Locale.US, "%.2f", hasilSm3Raw)

                                // Tampilkan ke layar
                                tvResultSM3.text = hasilFormatted
                                Toast.makeText(this@CalculatorActivity, "History Saved!", Toast.LENGTH_SHORT).show()
                            } else {
                                val msg = if (jsonObject.has("message")) jsonObject.get("message").asString else "Gagal hitung"
                                Toast.makeText(this@CalculatorActivity, msg, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@CalculatorActivity, "Error Data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}