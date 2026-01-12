package com.example.cradleapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ReportAdapter(
    private val context: Context,
    private var reportList: List<ReportModel>
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_transaksi, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // 1. Tampilan Ringkas di List (Konversi String ke Double untuk tampilan jika perlu)
        val pKirimVal = report.P_Kirim?.toDoubleOrNull() ?: 0.0
        val pAmbilVal = report.P_Ambil?.toDoubleOrNull() ?: 0.0

        holder.tvNomor.text = report.Cradle ?: "-"
        holder.tvTanggal.text = report.Tanggal ?: "-"
        holder.tvPengirim.text = "Customer: ${report.Customer ?: "Unknown"}"
        holder.tvTujuan.text = "P: $pKirimVal → $pAmbilVal Bar"

        // Format Rupiah untuk Revenue
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        holder.tvRevenue.text = formatRupiah.format(report.Revenue_IDR ?: 0.0)

        // 2. Klik Kartu -> Kirim SEMUA Data ke DetailReportActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailReportActivity::class.java)

            // Data Identitas (String)
            intent.putExtra("ID_TRANSAKSI", report.id_transaksi ?: "")
            intent.putExtra("CUSTOMER_NAME", report.Customer ?: "")
            intent.putExtra("CRADLE_NAME", report.Cradle ?: "")
            intent.putExtra("TANGGAL", report.Tanggal ?: "")

            // Data Teknis (WAJIB DOUBLE - Konversi dari String Model ke Double Intent)
            intent.putExtra("REVENUE", report.Revenue_IDR ?: 0.0)
            intent.putExtra("P_KIRIM", pKirimVal)
            intent.putExtra("P_AMBIL", pAmbilVal)

            // Data Hasil Perhitungan (Double)
            intent.putExtra("SM3_KIRIM", report.SM3_Kirim ?: 0.0)
            intent.putExtra("SM3_AMBIL", report.SM3_Ambil ?: 0.0)
            intent.putExtra("SM3_TOTAL", report.Nilai_SM3_Total ?: 0.0)
            intent.putExtra("FPV_TOTAL", report.Fpv_Total ?: 0.0)

            // Parameter AGA8 (Double)
            intent.putExtra("T_AWAL", report.T_Awal ?: 0.0)
            intent.putExtra("T_AKHIR", report.T_Akhir ?: 0.0)
            intent.putExtra("CO2", report.CO2 ?: 0.0)
            intent.putExtra("N2", report.N2 ?: 0.0)
            intent.putExtra("SG", report.SG ?: 0.0)
            intent.putExtra("HARGA", report.Harga ?: 0.0)

            context.startActivity(intent)
        }

        // 3. Tombol Delete
        holder.btnDelete.setOnClickListener {
            if (context is DataListActivity) {
                val idHapus = report.id_transaksi ?: ""
                if (idHapus.isNotEmpty()) {
                    context.konfirmasiHapus(idHapus)
                } else {
                    Toast.makeText(context, "ID Transaksi Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = reportList.size

    fun updateData(newList: List<ReportModel>) {
        reportList = newList
        notifyDataSetChanged()
    }

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvPengirim: TextView = itemView.findViewById(R.id.tvPengirim)
        val tvTujuan: TextView = itemView.findViewById(R.id.tvTujuan)
        val tvRevenue: TextView = itemView.findViewById(R.id.tvRevenue)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
}