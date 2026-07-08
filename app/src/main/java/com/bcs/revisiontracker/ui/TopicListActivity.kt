package com.bcs.revisiontracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.data.Topic
import com.bcs.revisiontracker.databinding.ActivityTopicListBinding
import com.bcs.revisiontracker.databinding.DialogAddTopicBinding
import com.bcs.revisiontracker.util.ReminderScheduler
import com.bcs.revisiontracker.util.SpacedRepetition

class TopicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicListBinding
    private lateinit var prefs: PrefsManager
    private lateinit var subjectName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: return finish()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = subjectName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        prefs = PrefsManager(this)
        binding.recyclerTopics.layoutManager = LinearLayoutManager(this)
        binding.fabAddTopic.setOnClickListener { showAddTopicDialog() }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val subjects = prefs.loadSubjects()
        val subject = prefs.findSubject(subjects, subjectName) ?: return
        binding.textEmpty.visibility = if (subject.topics.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerTopics.adapter = TopicAdapter(subject.topics) { topic ->
            MilestoneActivity.start(this, subjectName, topic.id)
        }
    }

    private fun showAddTopicDialog() {
        val dialogBinding = DialogAddTopicBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle(getString(com.bcs.revisiontracker.R.string.add_topic))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(com.bcs.revisiontracker.R.string.save)) { _, _ ->
                val title = dialogBinding.inputTopicTitle.text?.toString()?.trim().orEmpty()
                val category = dialogBinding.inputTopicCategory.text?.toString()?.trim()
                    .takeUnless { it.isNullOrEmpty() } ?: "General"
                if (title.isNotEmpty()) addTopic(title, category)
            }
            .setNegativeButton(getString(com.bcs.revisiontracker.R.string.cancel), null)
            .show()
    }

    private fun addTopic(title: String, category: String) {
        val subjects = prefs.loadSubjects()
        val subject = prefs.findSubject(subjects, subjectName) ?: return
        val topic = Topic(title = title, category = category)

        // Seed and schedule Round 1 ("+1 Day") immediately on creation.
        SpacedRepetition.initializeFirstRound(topic)
        subject.topics.add(topic)
        prefs.saveSubjects(subjects)

        val round1 = topic.milestones.first { it.round == 1 }
        ReminderScheduler.schedule(this, subjectName, topic.title, 1, round1.dueAtEpochMillis)

        refreshList()
    }

    companion object {
        private const val EXTRA_SUBJECT_NAME = "extra_subject_name"

        fun start(context: Context, subjectName: String) {
            val intent = Intent(context, TopicListActivity::class.java)
            intent.putExtra(EXTRA_SUBJECT_NAME, subjectName)
            context.startActivity(intent)
        }
    }
}
