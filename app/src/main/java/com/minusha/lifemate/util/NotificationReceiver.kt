package com.minusha.lifemate.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.minusha.lifemate.MainActivity
import com.minusha.lifemate.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "DAILY_REMINDER" -> showMoodReminder(context)
            "android.intent.action.BOOT_COMPLETED" -> {
                // Reschedule notifications after device reboot
                // This requires additional setup to save and restore notification preferences
            }
        }
    }

    private fun showMoodReminder(context: Context) {
        // Create the notification channel (required for Android 8.0+)
        createNotificationChannel(context)

        // Create intent for notification tap action
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_mood_entry", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure you have this icon
            .setContentTitle("LifeMate")
            .setContentText("How are you feeling today? Take a moment to record your mood")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mood Tracker"
            val descriptionText = "Daily mood tracking reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "mood_tracker_channel"
        private const val NOTIFICATION_ID = 1001
    }
}