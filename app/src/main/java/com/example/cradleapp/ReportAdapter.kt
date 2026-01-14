package com.example.cradleapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ReportAdapter(
    private val context: Context,
    private var reportList: List<ReportModel>
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        // Menggunakan layout item_data_list
        val view = LayoutInflater.from(context).inflate(R.layout.item_data_list, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // 1. Ambil dan Format Data Teknis
        val pKirimVal = report.P_Kirim?.toDoubleOrNull() ?: 0.0
        val pAmbilVal = report.P_Ambil?.toDoubleOrNull() ?: 0.0
        val sm3Total = report.Nilai_SM3_Total ?: 0.0

        // Pasang data ke UI Item List
        holder.tvNomor.text = "TRX-${report.id_transaksi ?: "000"}"
        holder.tvTanggal.text = report.Tanggal ?: "-"
        holder.tvCustomer.text = report.Customer ?: "Unknown Customer"

        // Data Grid Mini (Cradle, Pressure, Volume)
        holder.tvCradle.text = report.Cradle ?: "-"
        holder.tvPKirim.text = "${pKirimVal.toInt()} Bar"
        holder.tvPAmbil.text = "${pAmbilVal.toInt()} Bar"
        holder.tvSM3Total.text = String.format("%.2f M³", sm3Total)

        // Format Rupiah untuk Revenue
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        holder.tvRevenue.text = formatRupiah.format(report.Revenue_IDR ?: 0.0)

        // Klik Kartu -> Kirim SEMUA Data ke DetailReportActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailReportActivity::class.java)

            // Data Identitas
            intent.putExtra("ID_TRANSAKSI", report.id_transaksi ?: "")
            intent.putExtra("CUSTOMER_NAME", report.Customer ?: "")
            intent.putExtra("CRADLE_NAME", report.Cradle ?: "")
            intent.putExtra("TANGGAL", report.Tanggal ?: "")

            // Data Teknis Tekanan & Revenue
            intent.putExtra("REVENUE", report.Revenue_IDR ?: 0.0)
            intent.putExtra("P_KIRIM", pKirimVal)
            intent.putExtra("P_AMBIL", pAmbilVal)

            // Data Hasil Perhitungan Volume
            intent.putExtra("SM3_KIRIM", report.SM3_Kirim ?: 0.0)
            intent.putExtra("SM3_AMBIL", report.SM3_Ambil ?: 0.0)
            intent.putExtra("SM3_TOTAL", sm3Total)

            // Data FPV ambil dan kirim
            intent.putExtra("FPV_KIRIM", report.Fpv_Kirim ?: 0.0)
            intent.putExtra("FPV_AMBIL", report.Fpv_Ambil ?: 0.0)
            intent.putExtra("FPV_TOTAL", report.Fpv_Total ?: 0.0)

            // Parameter AGA8 & Suhu
            intent.putExtra("T_AWAL", report.T_Awal ?: 0.0)
            intent.putExtra("T_AKHIR", report.T_Akhir ?: 0.0)
            intent.putExtra("CO2", report.CO2 ?: 0.0)
            intent.putExtra("N2", report.N2 ?: 0.0)
            intent.putExtra("SG", report.SG ?: 0.0)
            intent.putExtra("HARGA", report.Harga ?: 0.0)

            context.startActivity(intent)
        }

        //Delete
        holder.btnDelete.setOnClickListener {
            if (context is DataListActivity) {
                val idHapus = report.id_transaksi ?: ""
                if (idHapus.isNotEmpty()) {
                    context.konfirmasiHapus(idHapus)
                }
            }
        }
    }

    override fun getItemCount(): Int = reportList.size

    fun updateData(newList: List<ReportModel>) {
        reportList = newList
        notifyDataSetChanged()
    }

    // ViewHolder diselaraskan dengan ID di item_data_list.xml
    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvCustomer: TextView = itemView.findViewById(R.id.tvCustomer)
        val tvCradle: TextView = itemView.findViewById(R.id.tvCradle)
        val tvPKirim: TextView = itemView.findViewById(R.id.tvPKirim)
        val tvPAmbil: TextView = itemView.findViewById(R.id.tvPAmbil)
        val tvSM3Total: TextView = itemView.findViewById(R.id.tvSM3Total)
        val tvRevenue: TextView = itemView.findViewById(R.id.tvRevenue)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
}