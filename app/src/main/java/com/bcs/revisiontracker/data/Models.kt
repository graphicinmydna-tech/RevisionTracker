package com.bcs.revisiontracker.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * The 9 compulsory BPSC (BCS Preliminary) syllabus subjects.
 * These are fixed and always seed the master dashboard.
 */
object CompulsorySubjects {
    val NAMES = listOf(
        "Bangladesh Affairs",
        "International Affairs",
        "Bangla Language & Literature",
        "English Language & Literature",
        "Mathematical Reasoning",
        "General Science",
        "Computer & Information Technology",
        "Moral, Values, Good Governance & Ethics",
        "Geography, Environment & Disaster Management"
    )
}

/** A single spaced-repetition milestone (Round 1..5) for a topic. */
data class Milestone(
    val round: Int,                 // 1..5
    val offsetDays: Int,            // +1, +3, +7, +14, +30
    var completed: Boolean = false,
    var completedAtEpochMillis: Long = 0L,
    var dueAtEpochMillis: Long = 0L // computed when previous round is completed / topic created
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("round", round)
        put("offsetDays", offsetDays)
        put("completed", completed)
        put("completedAt", completedAtEpochMillis)
        put("dueAt", dueAtEpochMillis)
    }

    companion object {
        fun fromJson(o: JSONObject): Milestone = Milestone(
            round = o.getInt("round"),
            offsetDays = o.getInt("offsetDays"),
            completed = o.optBoolean("completed", false),
            completedAtEpochMillis = o.optLong("completedAt", 0L),
            dueAtEpochMillis = o.optLong("dueAt", 0L)
        )

        val DEFAULT_OFFSETS = listOf(1, 3, 7, 14, 30) // Round 1..5
    }
}

/** A user-added sub-topic living inside a subject's topic list. */
data class Topic(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var category: String = "General", // free-form classification tag typed by the user
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val milestones: MutableList<Milestone> = Milestone.DEFAULT_OFFSETS.mapIndexed { idx, off ->
        Milestone(round = idx + 1, offsetDays = off)
    }.toMutableList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("category", category)
        put("createdAt", createdAtEpochMillis)
        put("milestones", JSONArray(milestones.map { it.toJson() }))
    }

    companion object {
        fun fromJson(o: JSONObject): Topic {
            val ms = mutableListOf<Milestone>()
            val arr = o.optJSONArray("milestones")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    ms.add(Milestone.fromJson(arr.getJSONObject(i)))
                }
            }
            return Topic(
                id = o.getString("id"),
                title = o.getString("title"),
                category = o.optString("category", "General"),
                createdAtEpochMillis = o.optLong("createdAt", System.currentTimeMillis()),
                milestones = ms
            )
        }
    }
}

/** One of the 9 compulsory subjects, holding its own list of topics. */
data class Subject(
    val name: String,
    val topics: MutableList<Topic> = mutableListOf()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("topics", JSONArray(topics.map { it.toJson() }))
    }

    companion object {
        fun fromJson(o: JSONObject): Subject {
            val topics = mutableListOf<Topic>()
            val arr = o.optJSONArray("topics")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    topics.add(Topic.fromJson(arr.getJSONObject(i)))
                }
            }
            return Subject(name = o.getString("name"), topics = topics)
        }
    }
}
