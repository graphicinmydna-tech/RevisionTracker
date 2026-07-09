package com.bcs.revisiontracker.util

import com.bcs.revisiontracker.data.Subject
import java.util.Calendar

data class DailyStats(val completed: Int, val total: Int) {
    val remaining: Int get() = total - completed
}

object RevisionStats {
    fun computeDailyStats(subjects: List<Subject>): DailyStats {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = startOfToday + 24L * 60 * 60 * 1000 - 1

        var completed = 0
        var total = 0

        subjects.forEach { subject ->
            subject.topics.forEach { topic ->
                topic.milestones.forEach { m ->
                    val pendingDueByToday = !m.completed && m.dueAtEpochMillis in 1..endOfToday
                    val completedToday = m.completed && m.completedAtEpochMillis in startOfToday..endOfToday
                    if (pendingDueByToday) total++
                    if (completedToday) { completed++; total++ }
                }
            }
        }
        return DailyStats(completed, total)
    }
}