package com.bcs.revisiontracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bcs.revisiontracker.R
import com.bcs.revisiontracker.data.Subject
import com.bcs.revisiontracker.util.RevisionStats
import com.google.android.material.card.MaterialCardView

class SubjectAdapter(
    private val subjects: List<Subject>,
    private val onClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val name: android.widget.TextView = view.findViewById(R.id.textSubjectName)
        val summary: android.widget.TextView = view.findViewById(R.id.textSubjectSummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val subject = subjects[position]
        holder.name.text = subject.name
        val topicCount = subject.topics.size
        val completedRounds = subject.topics.sumOf { t -> t.milestones.count { it.completed } }
        val totalRounds = topicCount * 5
        holder.summary.text = if (topicCount == 0)
            "No topics added yet"
        else
            "$topicCount topic(s) · $completedRounds/$totalRounds rounds complete"

        val strokeWidthPx = (2 * holder.itemView.resources.displayMetrics.density).toInt()
        if (RevisionStats.isSubjectDueToday(subject)) {
            holder.card.strokeWidth = strokeWidthPx
            holder.card.strokeColor = holder.itemView.context.getColor(R.color.due_alert)
        } else {
            holder.card.strokeWidth = 0
        }

        holder.itemView.setOnClickListener { onClick(subject) }
    }

    override fun getItemCount() = subjects.size
}