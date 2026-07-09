package com.bcs.revisiontracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PrefsManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSubjects(): MutableList<Subject> {
        val raw = prefs.getString(KEY_SUBJECTS, null)
        if (raw.isNullOrEmpty()) {
            val seeded = CompulsorySubjects.NAMES.map { Subject(name = it) }.toMutableList()
            saveSubjects(seeded)
            return seeded
        }
        val arr = JSONArray(raw)
        val list = mutableListOf<Subject>()
        for (i in 0 until arr.length()) {
            list.add(Subject.fromJson(arr.getJSONObject(i)))
        }
        return list
    }

    fun saveSubjects(subjects: List<Subject>) {
        val arr = JSONArray(subjects.map { it.toJson() })
        prefs.edit().putString(KEY_SUBJECTS, arr.toString()).apply()
    }

    fun findSubject(subjects: List<Subject>, name: String): Subject? =
        subjects.firstOrNull { it.name == name }

    fun findTopic(subjects: List<Subject>, subjectName: String, topicId: String): Topic? =
        findSubject(subjects, subjectName)?.topics?.firstOrNull { it.id == topicId }

    fun getReminderHour(): Int = prefs.getInt(KEY_REMINDER_HOUR, -1)
    fun getReminderMinute(): Int = prefs.getInt(KEY_REMINDER_MINUTE, -1)
    fun hasReminderTime(): Boolean = getReminderHour() != -1 && getReminderMinute() != -1

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "bcs_data"
        private const val KEY_SUBJECTS = "subjects_json"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
    }
}
