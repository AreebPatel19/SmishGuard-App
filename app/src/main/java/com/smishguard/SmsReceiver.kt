package com.smishguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.app.NotificationCompat

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val fullMessage = StringBuilder()
        var senderNumber = ""

        for (sms in messages) {
            senderNumber = sms.displayOriginatingAddress ?: "Unknown"
            fullMessage.append(sms.displayMessageBody)
        }

        val textContent = fullMessage.toString()
        val timestamp = ScanHistoryManager.getCurrentTime()

        // Tier 1: Whitelist Check
        if (isSavedContact(context, senderNumber)) {
            ScanHistoryManager.saveRecord(context, ScanRecord(senderNumber, textContent, false, timestamp))
            refreshAppUi(context)
            return
        }

        // Tier 2: ONNX AI Scan
        try {
            val detector = PhishingDetector(context)
            val isThreat = detector.isPhishing(textContent)

            ScanHistoryManager.saveRecord(context, ScanRecord(senderNumber, textContent, isThreat, timestamp))
            refreshAppUi(context)

            if (isThreat) {
                showSmishingAlert(context, senderNumber, textContent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isSavedContact(context: Context, phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) return true
            }
        } catch (e: Exception) { e.printStackTrace() }
        return false
    }

    private fun showSmishingAlert(context: Context, sender: String, body: String) {
        val channelId = "smishguard_alerts"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SmishGuard Alerts", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🚨 Threat Blocked!")
            .setContentText("Suspicious message from $sender")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun refreshAppUi(context: Context) {
        context.sendBroadcast(Intent("com.smishguard.UPDATE_HISTORY"))
    }
}