package com.smishguard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private var records: List<ScanRecord>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderText: TextView = view.findViewById(R.id.senderText)
        val timestampText: TextView = view.findViewById(R.id.timestampText)
        val messageText: TextView = view.findViewById(R.id.messageText)
        val statusBadge: TextView = view.findViewById(R.id.statusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // FIXED: Changed R.id to R.layout so it properly finds your XML file
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scan_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.senderText.text = record.sender
        holder.timestampText.text = record.timestamp
        holder.messageText.text = record.message

        if (record.isPhishing) {
            holder.statusBadge.text = "PHISHING"
            holder.statusBadge.setTextColor(Color.WHITE)
            holder.statusBadge.setBackgroundColor(Color.parseColor("#DC2626")) // Threat Red
        } else {
            holder.statusBadge.text = "SAFE"
            holder.statusBadge.setTextColor(Color.WHITE)
            holder.statusBadge.setBackgroundColor(Color.parseColor("#16A34A")) // Safe Green
        }
    }

    override fun getItemCount() = records.size

    fun updateData(newRecords: List<ScanRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}