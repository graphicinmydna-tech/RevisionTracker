package com.bcs.revisiontracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Single source of truth for local, offline persistence.
 * Everything is sandboxed inside the "bcs_data" SharedPreferences file
 * as a single JSON blob keyed by "subjects_json". No network, no external accounts.
 */
class PrefsManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSubjects(): MutableList<Subject> {
        val raw = prefs.getString(KEY_SUBJECTS, null)
        if (raw.isNullOrEmpty()) {
            // First launch: seed the 9 compulsory subjects with empty topic lists.
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

    companion object {
        private const val PREFS_NAME = "bcs_data"
        private const val KEY_SUBJECTS = "subjects_json"
    }
}
