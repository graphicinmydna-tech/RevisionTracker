package com.bcs.revisiontracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        prefs = PrefsManager(this)
        binding.recyclerSubjects.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        // Reload every time we return from a subject/topic screen so progress summaries stay fresh.
        val subjects = prefs.loadSubjects()
        binding.recyclerSubjects.adapter = SubjectAdapter(subjects) { subject ->
            TopicListActivity.start(this, subject.name)
        }
    }
}
