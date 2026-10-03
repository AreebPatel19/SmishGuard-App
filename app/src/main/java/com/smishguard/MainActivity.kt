package com.smishguard

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {
    private lateinit var detector: PhishingDetector
    private lateinit var adapter: HistoryAdapter

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadHistory()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        detector = PhishingDetector(this)

        // Setup History View
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        // Setup Manual Scan
        val smsInputField = findViewById<EditText>(R.id.smsInputField)
        findViewById<Button>(R.id.scanButton).setOnClickListener {
            val textToScan = smsInputField.text.toString()
            if (textToScan.isNotBlank()) {
                val isPhishing = detector.isPhishing(textToScan)
                val record = ScanRecord("Manual Sandbox", textToScan, isPhishing, ScanHistoryManager.getCurrentTime())
                ScanHistoryManager.saveRecord(this, record)
                smsInputField.text.clear()
                loadHistory()
            }
        }

        requestShieldPermissions()
        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, IntentFilter("com.smishguard.UPDATE_HISTORY"), Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(updateReceiver, IntentFilter("com.smishguard.UPDATE_HISTORY"))
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(updateReceiver)
    }

    private fun loadHistory() {
        val records = ScanHistoryManager.getHistory(this)
        adapter.updateData(records)
        findViewById<TextView>(R.id.totalScannedValue).text = records.size.toString()
        findViewById<TextView>(R.id.threatsBlockedValue).text = records.count { it.isPhishing }.toString()
    }

    private fun requestShieldPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needsPermissions = permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needsPermissions) ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
    }
}