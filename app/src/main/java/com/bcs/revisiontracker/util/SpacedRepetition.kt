package com.bcs.revisiontracker.util

import com.bcs.revisiontracker.data.Topic
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SpacedRepetition {

    private val dateFormat = SimpleDateFormat("EEE, d MMM yyyy, h:mm a", Locale.getDefault())

    fun formatDate(epochMillis: Long): String = dateFormat.format(epochMillis)

    fun addDaysAtTime(baseEpochMillis: Long, days: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = baseEpochMillis
        cal.add(Calendar.DAY_OF_YEAR, days)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun initializeFirstRound(topic: Topic, reminderHour: Int, reminderMinute: Int) {
        val round1 = topic.milestones.firstOrNull { it.round == 1 } ?: return
        if (round1.dueAtEpochMillis == 0L) {
            round1.dueAtEpochMillis = addDaysAtTime(System.currentTimeMillis(), round1.offsetDays, reminderHour, reminderMinute)
        }
    }

    fun completeRoundAndScheduleNext(topic: Topic, round: Int, reminderHour: Int, reminderMinute: Int): String? {
        val current = topic.milestones.firstOrNull { it.round == round } ?: return null
        current.completed = true
        current.completedAtEpochMillis = System.currentTimeMillis()

        val next = topic.milestones.firstOrNull { it.round == round + 1 } ?: return null
        next.dueAtEpochMillis = addDaysAtTime(current.completedAtEpochMillis, next.offsetDays, reminderHour, reminderMinute)
        return formatDate(next.dueAtEpochMillis)
    }
}