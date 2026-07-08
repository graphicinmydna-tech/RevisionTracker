package com.bcs.revisiontracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bcs.revisiontracker.util.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: return
        val topic = intent.getStringExtra(EXTRA_TOPIC) ?: return
        val round = intent.getIntExtra(EXTRA_ROUND, 1)
        NotificationHelper.notifyRevisionDue(context, subject, topic, round)
    }

    companion object {
        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_TOPIC = "extra_topic"
        const val EXTRA_ROUND = "extra_round"
    }
}
