package com.bcs.revisiontracker.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bcs.revisiontracker.receiver.ReminderReceiver

object ReminderScheduler {

    fun schedule(context: Context, subject: String, topic: String, round: Int, dueAtEpochMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_SUBJECT, subject)
            putExtra(ReminderReceiver.EXTRA_TOPIC, topic)
            putExtra(ReminderReceiver.EXTRA_ROUND, round)
        }
        val requestCode = (subject + topic + round).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueAtEpochMillis, pendingIntent)
            } else {
                // Fallback if the user hasn't granted exact-alarm permission on Android 12+.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueAtEpochMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueAtEpochMillis, pendingIntent)
        }
    }

    fun cancel(context: Context, subject: String, topic: String, round: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val requestCode = (subject + topic + round).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
