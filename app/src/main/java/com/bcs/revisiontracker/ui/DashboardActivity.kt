package com.bcs.revisiontracker.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bcs.revisiontracker.R
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.databinding.ActivityDashboardBinding
import java.util.Calendar

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

        if (!prefs.hasReminderTime()) {
            showReminderTimePicker(isFirstRun = true)
        }
    }

    override fun onResume() {
        super.onResume()
        val subjects = prefs.loadSubjects()
        binding.recyclerSubjects.adapter = SubjectAdapter(subjects) { subject ->
            TopicListActivity.start(this, subject.name)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_set_reminder_time) {
            showReminderTimePicker(isFirstRun = false)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showReminderTimePicker(isFirstRun: Boolean) {
        val cal = Calendar.getInstance()
        val currentHour = if (prefs.hasReminderTime()) prefs.getReminderHour() else cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = if (prefs.hasReminderTime()) prefs.getReminderMinute() else 0

        val dialog = TimePickerDialog(
            this,
            { _, hour, minute ->
                prefs.setReminderTime(hour, minute)
            },
            currentHour,
            currentMinute,
            false
        )
        if (isFirstRun) {
            dialog.setTitle(getString(R.string.pick_reminder_time_title))
            dialog.setCancelable(false)
        }
        dialog.show()
    }
}
