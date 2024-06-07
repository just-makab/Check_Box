package com.example.checkbox

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class timesheetEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleTextView: TextView = itemView.findViewById(R.id.recyclerTitle)
    val categoryTextView: TextView = itemView.findViewById(R.id.recyclerCategory)
    val dateTextView: TextView = itemView.findViewById(R.id.recyclerDate)
    val startTimeTextView: TextView = itemView.findViewById(R.id.recyclerStartTime)
    val endTimeTextView: TextView = itemView.findViewById(R.id.recyclerEndTime)
    val totalMinutesTextView: TextView = itemView.findViewById(R.id.recyclerTotalTime)
    val imageView: ImageView = itemView.findViewById(R.id.recyclerImage)

fun bind(timesheetEntry: TimesheetEntry) {
    titleTextView.text = timesheetEntry.title
    categoryTextView.text = timesheetEntry.category
    dateTextView.text = timesheetEntry.date
    startTimeTextView.text = timesheetEntry.startTime
    endTimeTextView.text = timesheetEntry.endTime

    val totalHours = timesheetEntry.totalMinutes / 60
    val totalMinutes = timesheetEntry.totalMinutes % 60

    totalMinutesTextView.text = "$totalHours hr $totalMinutes min"

    Glide.with(itemView)
        .load(timesheetEntry.imageUrl)
        .into(imageView)
    }
}
