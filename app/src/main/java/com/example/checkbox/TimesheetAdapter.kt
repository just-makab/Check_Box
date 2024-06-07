package com.example.checkbox

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TimesheetAdapter(
    private val context: Context,
    private val timesheetEntries: List<TimesheetEntry>
) : RecyclerView.Adapter<timesheetEntryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): timesheetEntryViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.timesheet_rv, parent, false)
        return timesheetEntryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: timesheetEntryViewHolder, position: Int) {
        val timesheetEntry = timesheetEntries[position]
        holder.bind(timesheetEntry)
    }

    override fun getItemCount(): Int {
        return timesheetEntries.size
    }
}