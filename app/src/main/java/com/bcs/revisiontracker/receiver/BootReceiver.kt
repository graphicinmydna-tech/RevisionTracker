package com.bcs.revisiontracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.util.ReminderScheduler

/**
 * Android clears exact alarms on reboot. Since every due-date is already stored
 * locally in bcs_data, we simply re-read the saved milestones and re-arm any
 * alarms that are still pending and in the future.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = PrefsManager(context)
        val subjects = prefs.loadSubjects()
        val now = System.currentTimeMillis()

        subjects.forEach { subject ->
            subject.topics.forEach { topic ->
                topic.milestones.forEach { milestone ->
                    if (!milestone.completed && milestone.dueAtEpochMillis > now) {
                        ReminderScheduler.schedule(
                            context, subject.name, topic.title,
                            milestone.round, milestone.dueAtEpochMillis
                        )
                    }
                }
            }
        }
    }
}
