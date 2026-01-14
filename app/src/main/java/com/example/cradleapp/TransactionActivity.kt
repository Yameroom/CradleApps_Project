package com.example.cradleapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TransactionActivity : AppCompatActivity() {

    // API Ngrok Server
    private val urlServer = "https://estimable-subfulgently-margarete.ngrok-free.dev"

    private lateinit var spinCustomer: Spinner
    private lateinit var spinCradle: Spinner
    private lateinit var etTglKirim: EditText
    private lateinit var etTglAmbil: EditText
    private lateinit var etPAwal: EditText
    private lateinit var etPAkhir: EditText
    private lateinit var btnSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        // Inisialisasi View
        spinCustomer = findViewById(R.id.spinCustomer)
        spinCradle = findViewById(R.id.spinCradle)
        etTglKirim = findViewById(R.id.etTglKirim)
        etTglAmbil = findViewById(R.id.etTglAmbil)
        etPAwal = findViewById(R.id.etPAwal)
        etPAkhir = findViewById(R.id.etPAkhir)
        btnSimpan = findViewById(R.id.btnSimpan)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        etTglKirim.setOnClickListener { showDatePicker(etTglKirim) }
        etTglAmbil.setOnClickListener { showDatePicker(etTglAmbil) }

        loadDataMaster()

        btnSimpan.setOnClickListener { simpanKeDatabase() }
    }

    private fun showDatePicker(editText: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            editText.setText(selectedDate)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadDataMaster() {
        fetchSpinnerData("get_customers.php", spinCustomer, "nama")
        fetchSpinnerData("get_trailers.php", spinCradle, "nama")
    }

    private fun fetchSpinnerData(fileName: String, spinner: Spinner, key: String) {
        val client = OkHttpClient()
        // 2. Tambahkan URL Ngrok dan Header Skip Warning pada GET master data
        val request = Request.Builder()
            .url("$urlServer/cradle_api/$fileName")
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@TransactionActivity, "Failed Connection Master to Server", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
                        if (jsonObject.has("status") && jsonObject.get("status").asString == "success") {
                            val dataArray = jsonObject.getAsJsonArray("data")
                            val list = ArrayList<String>()
                            for (i in 0 until dataArray.size()) {
                                list.add(dataArray[i].asJsonObject.get(key).asString)
                            }
                            runOnUiThread {
                                val adapter = ArrayAdapter(this@TransactionActivity, android.R.layout.simple_spinner_item, list)
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinner.adapter = adapter
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        })
    }

    private fun simpanKeDatabase() {
        val customer = spinCustomer.selectedItem?.toString() ?: ""
        val cradle = spinCradle.selectedItem?.toString() ?: ""
        val tglKirim = etTglKirim.text.toString()
        val tglAmbil = etTglAmbil.text.toString()

        val pAwalStr = etPAwal.text.toString().trim().replace(",", ".")
        val pAkhirStr = etPAkhir.text.toString().trim().replace(",", ".")

        // Validasi Form Kosong
        if (customer.isEmpty() || cradle.isEmpty() || tglKirim.isEmpty() || tglAmbil.isEmpty() || pAwalStr.isEmpty() || pAkhirStr.isEmpty()) {
            Toast.makeText(this, "Fill it, or I will make you bald", Toast.LENGTH_SHORT).show()
            return
        }

        val pAwal = pAwalStr.toDoubleOrNull() ?: 0.0
        val pAkhir = pAkhirStr.toDoubleOrNull() ?: 0.0

        // 2. LOGIKA VALIDASI TEKANAN
        val batasMaksimal = 500.0

        if (pAwal > batasMaksimal) {
            etPAwal.error = "Slow Down Mate, thats to high I think :V (Maks $batasMaksimal Bar)"
            etPAwal.requestFocus()
            return
        }

        if (pAkhir > pAwal) {
            etPAkhir.error = "Bro, are you know something name by math"
            etPAkhir.requestFocus()
            return
        }

        // Proses Kirim ke Server Ngrok
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("customer", customer)
            .add("craddle", cradle)
            .add("tgl_kirim", tglKirim)
            .add("tgl_ambil", tglAmbil)
            .add("p_awal", pAwalStr)
            .add("p_akhir", pAkhirStr)
            .build()

        val request = Request.Builder()
            .url("$urlServer/cradle_api/tambah_transaksi.php")
            .post(formBody)
            .addHeader("ngrok-skip-browser-warning", "true") // <--- Bypass Ngrok Warning
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@TransactionActivity, "Failed Connection to Server: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val responseData = body.string()
                    runOnUiThread {
                        if (responseData.contains("success")) {
                            Toast.makeText(this@TransactionActivity, "You did it mate", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this@TransactionActivity, "Failed: $responseData", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }
}