package com.bcs.revisiontracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bcs.revisiontracker.R
import com.bcs.revisiontracker.data.Topic
import com.bcs.revisiontracker.util.SpacedRepetition

class TopicAdapter(
    private val topics: List<Topic>,
    private val onClick: (Topic) -> Unit
) : RecyclerView.Adapter<TopicAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textTopicTitle)
        val category: TextView = view.findViewById(R.id.textTopicCategory)
        val nextDue: TextView = view.findViewById(R.id.textTopicNextDue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val topic = topics[position]
        holder.title.text = topic.title
        holder.category.text = "Category: ${topic.category}"

        val nextRound = topic.milestones.firstOrNull { !it.completed }
        holder.nextDue.text = if (nextRound != null && nextRound.dueAtEpochMillis > 0)
            "Round ${nextRound.round} due: ${SpacedRepetition.formatDate(nextRound.dueAtEpochMillis)}"
        else if (nextRound == null)
            "All 5 rounds complete"
        else
            "Not scheduled yet"

        holder.itemView.setOnClickListener { onClick(topic) }
    }

    override fun getItemCount() = topics.size
}
