package com.bcs.revisiontracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.databinding.ActivityMilestoneBinding
import com.bcs.revisiontracker.util.NotificationHelper
import com.bcs.revisiontracker.util.ReminderScheduler
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
        binding.recyclerMilestones.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val subjects = prefs.loadSubjects()
        val topic = prefs.findTopic(subjects, subjectName, topicId) ?: return finish()

        supportActionBar?.title = topic.title
        binding.textTopicHeader.text = "${topic.title}  ·  ${topic.category}"

        binding.recyclerMilestones.adapter = MilestoneAdapter(topic.milestones) { milestone, _ ->
            val nextDueLabel = SpacedRepetition.completeRoundAndScheduleNext(topic, milestone.round)
            prefs.saveSubjects(subjects)

            // Fire the immediate high-priority confirmation alert.
            NotificationHelper.notifyMilestoneCompleted(
                this, subjectName, topic.title, milestone.round, nextDueLabel
            )

            // Arm the alarm for the newly-unlocked next round, if any.
            val next = topic.milestones.firstOrNull { it.round == milestone.round + 1 }
            if (next != null && next.dueAtEpochMillis > 0) {
                ReminderScheduler.schedule(this, subjectName, topic.title, next.round, next.dueAtEpochMillis)
            }

            refresh()
        }
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
