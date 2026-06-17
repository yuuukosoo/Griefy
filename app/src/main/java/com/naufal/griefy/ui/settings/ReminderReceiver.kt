package com.naufal.griefy.ui.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.naufal.griefy.MainActivity
import com.naufal.griefy.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Griefy::ReminderWakeLock")
        wakeLock.acquire(3000)

        val reminderId = intent?.getIntExtra("REMINDER_ID", 101) ?: 101
        val title = intent?.getStringExtra("REMINDER_TITLE") ?: "Hari Peringatan Penting ✨"
        val desc = intent?.getStringExtra("REMINDER_DESC") ?: "Mari luangkan waktu sejenak untuk mengingat kenangan indah."

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


        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reminderId, notification)
    }
}