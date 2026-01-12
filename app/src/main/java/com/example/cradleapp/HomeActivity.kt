package com.example.cradleapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Pastikan ID ini sama dengan yang ada di XML kamu
        val cardInput = findViewById<CardView>(R.id.cardTransaksi)
        val cardList = findViewById<CardView>(R.id.cardViewData)

        // Klik untuk ke halaman Input
        cardInput.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

        // Klik untuk ke halaman List (Point 2)
        cardList.setOnClickListener {
            val intent = Intent(this, DataListActivity::class.java)
            startActivity(intent)
        }
    }
}