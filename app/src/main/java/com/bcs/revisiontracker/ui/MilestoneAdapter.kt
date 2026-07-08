package com.bcs.revisiontracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bcs.revisiontracker.R
import com.bcs.revisiontracker.data.Milestone
import com.bcs.revisiontracker.util.SpacedRepetition

class MilestoneAdapter(
    private val milestones: List<Milestone>,
    private val onToggle: (Milestone, Boolean) -> Unit
) : RecyclerView.Adapter<MilestoneAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val check: CheckBox = view.findViewById(R.id.checkMilestoneDone)
        val label: TextView = view.findViewById(R.id.textRoundLabel)
        val date: TextView = view.findViewById(R.id.textRoundDate)
        val status: TextView = view.findViewById(R.id.textRoundStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_milestone, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = milestones[position]
        holder.label.text = "Round ${m.round}  (+${m.offsetDays} day${if (m.offsetDays > 1) "s" else ""})"

        // A round can only be checked off once it actually has a due date,
        // i.e. the previous round has been completed (or it's Round 1).
        val unlocked = m.dueAtEpochMillis > 0
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = m.completed
        holder.check.isEnabled = unlocked && !m.completed

        holder.date.text = when {
            m.completed -> "Completed ${SpacedRepetition.formatDate(m.completedAtEpochMillis)}"
            unlocked -> "Due ${SpacedRepetition.formatDate(m.dueAtEpochMillis)}"
            else -> "Locked — finish the previous round first"
        }

        holder.status.text = if (m.completed) "DONE" else if (unlocked) "PENDING" else ""
        holder.status.setTextColor(
            holder.itemView.context.getColor(if (m.completed) R.color.done else R.color.pending)
        )

        holder.check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !m.completed) {
                onToggle(m, true)
            }
        }
    }

    override fun getItemCount() = milestones.size
}
