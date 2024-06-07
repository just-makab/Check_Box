package com.example.checkbox

data class TimesheetEntry(
    val title: String,
    val category: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val totalMinutes: Int,
    val imageUrl: String
)
