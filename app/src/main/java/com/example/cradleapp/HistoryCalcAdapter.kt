package com.example.cradleapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class HistoryCalcAdapter(private val list: List<HistoryCalc>) : RecyclerView.Adapter<HistoryCalcAdapter.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvDate: TextView = v.findViewById(R.id.tvHistDate)
        val tvPress: TextView = v.findViewById(R.id.tvHistPress)
        val tvTemp: TextView = v.findViewById(R.id.tvHistTemp)
        val tvResult: TextView = v.findViewById(R.id.tvHistResult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_history_calc, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.tvDate.text = data.created_at
        holder.tvPress.text = "${data.pressure_bar} Bar"
        holder.tvTemp.text = "${data.temp_c} °C"
        holder.tvResult.text = String.format(Locale.US, "%.2f", data.hasil_sm3)
    }

    override fun getItemCount(): Int = list.size
}