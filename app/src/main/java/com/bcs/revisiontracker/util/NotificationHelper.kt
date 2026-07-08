package com.bcs.revisiontracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bcs.revisiontracker.R

object NotificationHelper {

    const val CHANNEL_ID = "bcs_revision_reminders"
    private const val CHANNEL_NAME = "Revision Reminders"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts confirming milestone check-offs and upcoming revision rounds."
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /** Immediate confirmation alert fired the moment a milestone is checked off. */
    fun notifyMilestoneCompleted(context: Context, subject: String, topic: String, round: Int, nextDueLabel: String?) {
        ensureChannel(context)
        val title = "Round $round complete: $topic"
        val text = if (nextDueLabel != null)
            "$subject — next revision scheduled for $nextDueLabel"
        else
            "$subject — all 5 revision rounds complete!"

        show(context, id = (subject + topic + round).hashCode(), title = title, text = text)
    }

    /** Alert fired by the AlarmManager when a scheduled revision date arrives. */
    fun notifyRevisionDue(context: Context, subject: String, topic: String, round: Int) {
        ensureChannel(context)
        show(
            context,
            id = (subject + topic + "due" + round).hashCode(),
            title = "Revision due: $topic",
            text = "$subject — Round $round is due today. Open Revision Tracker to review."
        )
    }

    private fun show(context: Context, id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
