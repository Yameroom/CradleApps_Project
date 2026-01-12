package com.example.cradleapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransaksiAdapter(private val listTransaksi: List<Transaksi>) :
    RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder>() {

    class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
        val tvPengirim: TextView = itemView.findViewById(R.id.tvPengirim)
        val tvTujuan: TextView = itemView.findViewById(R.id.tvTujuan)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi, parent, false)
        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        val item = listTransaksi[position]

        // Menampilkan data ke layar
        holder.tvNomor.text = item.nomor_cradle
        holder.tvPengirim.text = "Pengirim: ${item.nama_pengirim}"
        holder.tvTujuan.text = "Tujuan: ${item.tujuan}"
        holder.tvTanggal.text = item.tanggal_kirim

        // --- LOGIKA EDIT (Klik seluruh kartu) ---
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditActivity::class.java)

            // Kirim data lama ke EditActivity agar form otomatis terisi
            intent.putExtra("ID", item.id)
            intent.putExtra("NOMOR", item.nomor_cradle)
            intent.putExtra("PENGIRIM", item.nama_pengirim)
            intent.putExtra("TUJUAN", item.tujuan)

            context.startActivity(intent)
        }

        // --- LOGIKA HAPUS (Hanya klik tombol sampah) ---
        holder.btnDelete.setOnClickListener {
            val context = holder.itemView.context
            if (context is DataListActivity) {
                context.konfirmasiHapus(item.id, position)
            }
        }
    }

    override fun getItemCount(): Int = listTransaksi.size
}