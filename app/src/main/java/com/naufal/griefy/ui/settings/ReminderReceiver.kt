package com.naufal.griefy.ui.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat


class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "griefy_reminder_channel"


        val channel = NotificationChannel(
            channelId,
            "Pengingat Hari Peringatan",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel untuk notifikasi pengingat Griefy"
        }
        notificationManager.createNotificationChannel(channel)


        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Hari yang Spesial ✨")
            .setContentText("Mari luangkan waktu sejenak untuk mengingat kembali kenangan indah hari ini.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()


        notificationManager.notify(101, notification)
    }
}