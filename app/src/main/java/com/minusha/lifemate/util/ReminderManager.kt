package com.minusha.lifemate.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import java.util.Calendar

object ReminderManager {

    private const val PREF_NAME = "reminder_preferences"
    private const val PREF_REMINDER_HOUR = "reminder_hour"
    private const val PREF_REMINDER_MINUTE = "reminder_minute"
    private const val PREF_REMINDER_ENABLED = "reminder_enabled"

    // Default reminder time: 8:00 PM
    private const val DEFAULT_HOUR = 20
    private const val DEFAULT_MINUTE = 0

    fun scheduleReminder(context: Context) {
        val prefs = getPreferences(context)
        val isEnabled = prefs.getBoolean(PREF_REMINDER_ENABLED, true)

        if (!isEnabled) {
            Log.d("ReminderManager", "Reminders are disabled, not scheduling")
            return
        }

        val hour = prefs.getInt(PREF_REMINDER_HOUR, DEFAULT_HOUR)
        val minute = prefs.getInt(PREF_REMINDER_MINUTE, DEFAULT_MINUTE)

        // Create an intent for the alarm
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.minusha.lifemate.MOOD_REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set up the alarm time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // If the time is already passed for today, set it for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Schedule the alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Log.d("ReminderManager", "Scheduled reminder for ${hour}:${minute}")
    }

    fun cancelReminder(context: Context) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.minusha.lifemate.MOOD_REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.d("ReminderManager", "Cancelled reminder")
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int, enabled: Boolean = true) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            putInt(PREF_REMINDER_HOUR, hour)
            putInt(PREF_REMINDER_MINUTE, minute)
            putBoolean(PREF_REMINDER_ENABLED, enabled)
            apply()
        }

        if (enabled) {
            cancelReminder(context)
            scheduleReminder(context)
        } else {
            cancelReminder(context)
        }
    }

    fun getReminderHour(context: Context): Int {
        return getPreferences(context).getInt(PREF_REMINDER_HOUR, DEFAULT_HOUR)
    }

    fun getReminderMinute(context: Context): Int {
        return getPreferences(context).getInt(PREF_REMINDER_MINUTE, DEFAULT_MINUTE)
    }

    fun isReminderEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(PREF_REMINDER_ENABLED, true)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}