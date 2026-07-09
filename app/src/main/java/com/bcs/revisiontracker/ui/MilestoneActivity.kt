package com.bcs.revisiontracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bcs.revisiontracker.R
import com.bcs.revisiontracker.data.Milestone
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.data.Subject
import com.bcs.revisiontracker.data.Topic
import com.bcs.revisiontracker.databinding.ActivityMilestoneBinding
import com.bcs.revisiontracker.util.NotificationHelper
import com.bcs.revisiontracker.util.SpacedRepetition

class MilestoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMilestoneBinding
    private lateinit var prefs: PrefsManager
    private lateinit var subjectName: String
    private lateinit var topicId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMilestoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: return finish()
        topicId = intent.getStringExtra(EXTRA_TOPIC_ID) ?: return finish()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        prefs = PrefsManager(this)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val subjects = prefs.loadSubjects()
        val topic = prefs.findTopic(subjects, subjectName, topicId) ?: return finish()

        supportActionBar?.title = ""
        binding.textTopicHeader.text = topic.title
        binding.textTopicCategory.text = topic.category

        binding.containerMilestones.removeAllViews()

        val finished = topic.milestones.filter { it.completed }
        val revision = topic.milestones.filter { !it.completed }

        if (finished.isNotEmpty()) {
            addSectionHeader("Finished")
            finished.forEach { addMilestoneRow(it, topic, subjects) }
        }
        if (revision.isNotEmpty()) {
            addSectionHeader("Revision")
            revision.forEach { addMilestoneRow(it, topic, subjects) }
        }
    }

    private fun addSectionHeader(title: String) {
        val header = layoutInflater.inflate(R.layout.item_section_header, binding.containerMilestones, false) as TextView
        header.text = title
        binding.containerMilestones.addView(header)
    }

    private fun addMilestoneRow(milestone: Milestone, topic: Topic, subjects: List<Subject>) {
        val row = layoutInflater.inflate(R.layout.item_milestone, binding.containerMilestones, false)
        val check = row.findViewById<CheckBox>(R.id.checkMilestoneDone)
        val label = row.findViewById<TextView>(R.id.textRoundLabel)
        val date = row.findViewById<TextView>(R.id.textRoundDate)
        val status = row.findViewById<TextView>(R.id.textRoundStatus)

        label.text = "Round ${milestone.round}  (+${milestone.offsetDays} day${if (milestone.offsetDays > 1) "s" else ""})"

        val unlocked = milestone.dueAtEpochMillis > 0
        check.setOnCheckedChangeListener(null)
        check.isChecked = milestone.completed
        check.isEnabled = unlocked && !milestone.completed

        date.text = when {
            milestone.completed -> "Completed ${SpacedRepetition.formatDate(milestone.completedAtEpochMillis)}"
            unlocked -> "Due ${SpacedRepetition.formatDate(milestone.dueAtEpochMillis)}"
            else -> "Locked — finish the previous round first"
        }

        status.text = if (milestone.completed) "DONE" else if (unlocked) "PENDING" else ""
        status.setTextColor(getColor(if (milestone.completed) R.color.done else R.color.pending))

        check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !milestone.completed) {
                val hour = if (prefs.hasReminderTime()) prefs.getReminderHour() else 8
                val minute = if (prefs.hasReminderTime()) prefs.getReminderMinute() else 0
                val nextDueLabel = SpacedRepetition.completeRoundAndScheduleNext(topic, milestone.round, hour, minute)
                prefs.saveSubjects(subjects)

                NotificationHelper.notifyMilestoneCompleted(
                    this, subjectName, topic.title, milestone.round, nextDueLabel
                )

                refresh()
            }
        }

        binding.containerMilestones.addView(row)
    }

    companion object {
        private const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        private const val EXTRA_TOPIC_ID = "extra_topic_id"

        fun start(context: Context, subjectName: String, topicId: String) {
            val intent = Intent(context, MilestoneActivity::class.java)
            intent.putExtra(EXTRA_SUBJECT_NAME, subjectName)
            intent.putExtra(EXTRA_TOPIC_ID, topicId)
            context.startActivity(intent)
        }
    }
}