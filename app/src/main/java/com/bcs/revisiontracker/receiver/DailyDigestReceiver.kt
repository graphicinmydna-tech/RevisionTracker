package com.bcs.revisiontracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.util.DailyDigestScheduler
import com.bcs.revisiontracker.util.NotificationHelper

class DailyDigestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PrefsManager(context)
        val subjects = prefs.loadSubjects()
        val now = System.currentTimeMillis()

        val dueDescriptions = mutableListOf<String>()
        subjects.forEach { subject ->
            subject.topics.forEach { topic ->
                val due = topic.milestones.firstOrNull { !it.completed && it.dueAtEpochMillis in 1..now }
                if (due != null) {
                    dueDescriptions.add("${subject.name}: ${topic.title} (Round ${due.round})")
                }
            }
        }

        NotificationHelper.notifyDailyDigest(context, dueDescriptions)

        val hour = if (prefs.hasReminderTime()) prefs.getReminderHour() else 8
        val minute = if (prefs.hasReminderTime()) prefs.getReminderMinute() else 0
        DailyDigestScheduler.scheduleNextDay(context, hour, minute)
    }
}