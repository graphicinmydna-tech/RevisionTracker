package com.bcs.revisiontracker.ui

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bcs.revisiontracker.R
import com.bcs.revisiontracker.data.PrefsManager
import com.bcs.revisiontracker.databinding.ActivityDashboardBinding
import com.bcs.revisiontracker.util.DailyDigestScheduler
import com.bcs.revisiontracker.util.RevisionStats
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var prefs: PrefsManager

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) exportDataTo(uri)
    }
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importDataFrom(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        prefs = PrefsManager(this)
        binding.recyclerSubjects.layoutManager = LinearLayoutManager(this)

        if (!prefs.hasReminderTime()) {
            showReminderTimePicker(isFirstRun = true)
        } else {
            DailyDigestScheduler.schedule(this, prefs.getReminderHour(), prefs.getReminderMinute())
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSubjects()
    }

    private fun refreshSubjects() {
        val subjects = prefs.loadSubjects()
        binding.recyclerSubjects.adapter = SubjectAdapter(subjects) { subject ->
            TopicListActivity.start(this, subject.name)
        }

        val stats = RevisionStats.computeDailyStats(subjects)
        binding.textRevisionsLeftToday.text = "${stats.completed}/${stats.total} revisions left"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_set_reminder_time -> {
                showReminderTimePicker(isFirstRun = false)
                true
            }
            R.id.action_export -> {
                exportLauncher.launch("revision_tracker_backup.json")
                true
            }
            R.id.action_import -> {
                importLauncher.launch(arrayOf("application/json"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportDataTo(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { out ->
                out.write(prefs.exportRawJson().toByteArray())
            }
            Toast.makeText(this, "Backup saved.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importDataFrom(uri: Uri) {
        try {
            val json = contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            if (json != null && prefs.importRawJson(json)) {
                Toast.makeText(this, "Backup restored.", Toast.LENGTH_SHORT).show()
                refreshSubjects()
            } else {
                Toast.makeText(this, "That file doesn't look like a valid backup.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showReminderTimePicker(isFirstRun: Boolean) {
        val cal = Calendar.getInstance()
        val currentHour = if (prefs.hasReminderTime()) prefs.getReminderHour() else cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = if (prefs.hasReminderTime()) prefs.getReminderMinute() else 0

        val dialog = TimePickerDialog(
            this,
            { _, hour, minute ->
                prefs.setReminderTime(hour, minute)
                DailyDigestScheduler.schedule(this, hour, minute)
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