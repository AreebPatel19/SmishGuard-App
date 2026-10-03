package com.smishguard

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanRecord(
    val sender: String,
    val message: String,
    val isPhishing: Boolean,
    val timestamp: String
)

object ScanHistoryManager {
    private const val PREFS_NAME = "smishguard_history"
    private const val KEY_RECORDS = "records"

    fun saveRecord(context: Context, record: ScanRecord) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val list = getHistory(context).toMutableList()
        list.add(0, record) // Add newest to the top
        val trimmedList = if (list.size > 100) list.subList(0, 100) else list // Store max 100

        val jsonArray = JSONArray()
        for (item in trimmedList) {
            val obj = JSONObject().apply {
                put("sender", item.sender)
                put("message", item.message)
                put("isPhishing", item.isPhishing)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_RECORDS, jsonArray.toString()).apply()
    }

    fun getHistory(context: Context): List<ScanRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val list = mutableListOf<ScanRecord>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ScanRecord(
                        sender = obj.optString("sender", "Unknown"),
                        message = obj.optString("message", ""),
                        isPhishing = obj.optBoolean("isPhishing", false),
                        timestamp = obj.optString("timestamp", "")
                    )
                )
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}