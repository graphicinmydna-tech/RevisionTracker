package com.bcs.revisiontracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.util.DailyDigestScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = PrefsManager(context)
        if (prefs.hasReminderTime()) {
            DailyDigestScheduler.schedule(context, prefs.getReminderHour(), prefs.getReminderMinute())
        }
    }
}