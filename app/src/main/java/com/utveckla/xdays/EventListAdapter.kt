package com.utveckla.xdays

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.utveckla.xdays.db.Event
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EventListAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventListAdapter.EventListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListViewHolder {
        return EventListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.event_layout, parent, false)
        )
    }

    inner class EventListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventNameView: TextView = itemView.findViewById(R.id.event_name)
        val eventDateView: TextView = itemView.findViewById(R.id.event_date)
    }

    override fun onBindViewHolder(holder: EventListViewHolder, position: Int) {
        val current = events[position]

        val dateStr = current.date
        val sdf = SimpleDateFormat("yyyy/MM/dd")
        val date: Date = sdf.parse(dateStr)
        val currentDate = sdf.format(Date())
        val rightNow: Date = sdf.parse(currentDate)
        var tense = "since"

        var diff: Long = rightNow.time - date.time

        if (diff < 0) {
            diff = -diff
            tense = "till"
        }

        holder.eventNameView.text = current.name
        holder.eventDateView.text = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toString() + " days " + tense + " " + dateStr

        holder.itemView.setOnClickListener{
            val bundle = bundleOf(
                Pair("id", current.id.toString()),
                Pair("name", current.name),
                Pair("date", current.date)
            )

            Navigation.findNavController(it).navigate(R.id.action_EventList_to_EditEvent, bundle)
        }
     }

    override fun getItemCount() = events.size
}