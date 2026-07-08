package com.bcs.revisiontracker.util

import com.bcs.revisiontracker.data.Topic
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SpacedRepetition {

    private val dateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())

    fun formatDate(epochMillis: Long): String = dateFormat.format(epochMillis)

    fun addDays(baseEpochMillis: Long, days: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = baseEpochMillis
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }

    /**
     * Call once when a topic is first created: seeds Round 1's due date as
     * "today + 1 day". Later rounds get their due date only once the prior
     * round is completed (chained scheduling), matching the milestone rules.
     */
    fun initializeFirstRound(topic: Topic) {
        val round1 = topic.milestones.firstOrNull { it.round == 1 } ?: return
        if (round1.dueAtEpochMillis == 0L) {
            round1.dueAtEpochMillis = addDays(System.currentTimeMillis(), round1.offsetDays)
        }
    }

    /**
     * Call when a round is checked off: marks it complete and, if there is a
     * next round, computes its due date as (completion time + that round's offset).
     * Returns a human-readable label for the next due date, or null if the
     * 5th round was just completed.
     */
    fun completeRoundAndScheduleNext(topic: Topic, round: Int): String? {
        val current = topic.milestones.firstOrNull { it.round == round } ?: return null
        current.completed = true
        current.completedAtEpochMillis = System.currentTimeMillis()

        val next = topic.milestones.firstOrNull { it.round == round + 1 } ?: return null
        next.dueAtEpochMillis = addDays(current.completedAtEpochMillis, next.offsetDays)
        return formatDate(next.dueAtEpochMillis)
    }
}
