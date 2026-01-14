package com.example.cradleapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class EditActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_edit)

        spinCustomer = findViewById(R.id.spinEditCustomer)
        spinCradle = findViewById(R.id.spinEditCradle)
        etTglKirim = findViewById(R.id.etEditTglKirim)
        etTglAmbil = findViewById(R.id.etEditTglAmbil)
        etPAwal = findViewById(R.id.etEditPAwal)
        etPAkhir = findViewById(R.id.etEditPAkhir)
        btnSimpan = findViewById(R.id.btnSimpanEdit)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val idTrx = intent.getStringExtra("ID_TRANSAKSI")
        val customerLama = intent.getStringExtra("CUSTOMER_NAME") ?: ""
        val cradleLama = intent.getStringExtra("CRADLE_NAME") ?: ""
        val tglKirimLama = intent.getStringExtra("TANGGAL_KIRIM") ?: ""
        val tglAmbilLama = intent.getStringExtra("TANGGAL_AMBIL") ?: ""

        etTglKirim.setText(tglKirimLama)
        etTglAmbil.setText(tglAmbilLama)

        val pKirim = intent.getDoubleExtra("P_KIRIM", 0.0)
        val pAmbil = intent.getDoubleExtra("P_AMBIL", 0.0)

        etPAwal.setText(pKirim.toString())
        etPAkhir.setText(pAmbil.toString())

        etTglKirim.setOnClickListener { showDatePicker(etTglKirim) }
        etTglAmbil.setOnClickListener { showDatePicker(etTglAmbil) }

        loadDataMaster(customerLama, cradleLama)

        btnSimpan.setOnClickListener {
            if (!idTrx.isNullOrEmpty()) {
                prosesUpdate(idTrx)
            } else {
                Toast.makeText(this, "Transaction ID not found, please try again mate!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(editText: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            editText.setText(selectedDate)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadDataMaster(customerLama: String, cradleLama: String) {
        fetchSpinnerData("get_customers.php", spinCustomer, "nama", customerLama)
        fetchSpinnerData("get_trailers.php", spinCradle, "nama", cradleLama)
    }

    private fun fetchSpinnerData(fileName: String, spinner: Spinner, key: String, defaultValue: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$urlServer/cradle_api/$fileName")
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FETCH_MASTER", "Owh, bad luck mate $fileName: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                response.body()?.use { body ->
                    val json = body.string()
                    try {
                        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
                        if (jsonObject.has("status") && jsonObject.get("status").asString == "success") {
                            val dataArray = jsonObject.getAsJsonArray("data")
                            val list = ArrayList<String>()
                            var selectedIndex = 0

                            for (i in 0 until dataArray.size()) {
                                val item = dataArray[i].asJsonObject.get(key).asString
                                list.add(item)
                                if (item.trim().equals(defaultValue.trim(), ignoreCase = true)) {
                                    selectedIndex = i
                                }
                            }

                            runOnUiThread {
                                val adapter = ArrayAdapter(this@EditActivity, android.R.layout.simple_spinner_item, list)
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinner.adapter = adapter
                                spinner.setSelection(selectedIndex)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("JSON_ERROR", "Parsing error $fileName: ${e.message}")
                    }
                }
            }
        })
    }

    private fun prosesUpdate(idTrx: String) {
        val pAwalStr = etPAwal.text.toString().trim().replace(",", ".")
        val pAkhirStr = etPAkhir.text.toString().trim().replace(",", ".")
        val tglKirim = etTglKirim.text.toString().trim()
        val tglAmbil = etTglAmbil.text.toString().trim()

        if (pAwalStr.isEmpty() || pAkhirStr.isEmpty() || tglKirim.isEmpty() || tglAmbil.isEmpty()) {
            Toast.makeText(this, "Fill it, or I will make you bald", Toast.LENGTH_SHORT).show()
            return
        }

        val pAwal = pAwalStr.toDoubleOrNull() ?: 0.0
        val pAkhir = pAkhirStr.toDoubleOrNull() ?: 0.0

        if (pAwal > 500.0) {
            etPAwal.error = "All people make mistake, I understand mate :)"
            return
        }
        if (pAkhir > pAwal) {
            etPAkhir.error = "You should start study about math buddy"
            return
        }

        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("id", idTrx)
            .add("customer", spinCustomer.selectedItem.toString())
            .add("craddle", spinCradle.selectedItem.toString())
            .add("p_awal", pAwalStr)
            .add("p_akhir", pAkhirStr)
            .add("tgl_kirim", tglKirim)
            .add("tgl_ambil", tglAmbil)
            .build()

        // 3. Tambahkan header skip-warning pada POST Request
        val request = Request.Builder()
            .url("$urlServer/cradle_api/update_data.php")
            .post(formBody)
            .addHeader("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@EditActivity, "Server is Offline/Trouble", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                val resStr = response.body()?.string() ?: ""
                Log.d("UPDATE_RESPONSE", resStr)

                runOnUiThread {
                    if (resStr.contains("success")) {
                        Toast.makeText(this@EditActivity, "You dit it mate", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditActivity, "Owh bad luck buddy, Im sorry :( $resStr", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}