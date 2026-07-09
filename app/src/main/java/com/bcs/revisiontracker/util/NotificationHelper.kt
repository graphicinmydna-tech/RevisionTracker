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
    private const val DAILY_DIGEST_ID = 8888

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts confirming milestone check-offs and daily revision summaries."
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * ONE daily notification summarizing everything due today, e.g.
     * "3 revisions left today" with each topic listed on its own line —
     * instead of a separate alert per topic.
     */
    fun notifyDailyDigest(context: Context, dueItems: List<String>) {
        if (dueItems.isEmpty()) return // nothing due — stay silent
        ensureChannel(context)

        val title = if (dueItems.size == 1) "1 revision left today" else "${dueItems.size} revisions left today"

        val style = NotificationCompat.InboxStyle()
        dueItems.take(6).forEach { style.addLine(it) }
        if (dueItems.size > 6) style.setSummaryText("+ ${dueItems.size - 6} more")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(dueItems.joinToString(", "))
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(DAILY_DIGEST_ID, notification)
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