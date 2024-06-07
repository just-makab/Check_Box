package com.example.checkbox

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class Timeline : AppCompatActivity() {

    lateinit var bottomNav: BottomNavigationView
    private lateinit var addTimeSheetButton: Button
    private lateinit var textDate: EditText
    private lateinit var timeDisplay: TextView
    private lateinit var dateDisplay: TextView
    private lateinit var fStore: FirebaseFirestore

    private lateinit var timesheetRecyclerView: RecyclerView
    private lateinit var timesheetAdapter: TimesheetAdapter
    private lateinit var timesheetEntries: List<TimesheetEntry>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timeline)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fStore = FirebaseFirestore.getInstance()

        timesheetRecyclerView = findViewById(R.id.timesheetRecyclerView)
        timesheetRecyclerView.layoutManager = LinearLayoutManager(this)
        timesheetEntries = mutableListOf()
        timesheetAdapter = TimesheetAdapter(this, timesheetEntries)
        timesheetRecyclerView.adapter = timesheetAdapter


        bottomNav = findViewById(R.id.bottomNavigationView)
        when (this::class.java) {
            Timeline::class.java -> bottomNav.menu.findItem(R.id.timeline).isChecked = true
        }
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.category -> {
                    val intent = Intent(this, Category::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.timeline -> {
                    val intent = Intent(this, Timeline::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> {
                    false
                }
            }
        }

        dateDisplay = findViewById(R.id.DateDisplay)
        timeDisplay = findViewById(R.id.TimeDisplay)
        updateTime()

        textDate = findViewById(R.id.editTextDate)
        textDate.setOnClickListener {
            showDatePicker()
        }



        addTimeSheetButton = findViewById(R.id.addTimesheetButton)

        addTimeSheetButton.setOnClickListener() {
            val intent = Intent(this, AddTimeSheet::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateTime() {
        val currentTime = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(currentTime)
        timeDisplay.text = formattedTime
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentTime)
        dateDisplay.text = formattedDate

    }

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { view, year, monthOfYear, dayOfMonth ->
            val selectedDate = String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth)
            textDate.setText(selectedDate)
            displayTimesheetEntries(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun displayTimesheetEntries(selectedDate: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val timesheetCollection =
                fStore.collection("users").document(uid).collection("timesSheets")

            if (selectedDate.isEmpty()) {
                timesheetCollection
                    .get()
                    .addOnSuccessListener { documents ->
                        val timesheetEntries = mutableListOf<TimesheetEntry>()
                        for (document in documents) {
                            val title = document.getString("title") ?: ""
                            val category = document.getString("category") ?: ""
                            val date = document.getString("date") ?: ""
                            val startTime = document.getString("startTime") ?: ""
                            val endTime = document.getString("endTime") ?: ""
                            val totalMinutes = document.getLong("totalMinutes")?.toInt() ?: 0
                            val imageUrl = document.getString("imageUrl") ?: ""

                            val timesheetEntry = TimesheetEntry(
                                title,
                                category,
                                date,
                                startTime,
                                endTime,
                                totalMinutes,
                                imageUrl
                            )
                            timesheetEntries.add(timesheetEntry)
                        }

                        val adapter = TimesheetAdapter(this, timesheetEntries)
                        timesheetRecyclerView.adapter = adapter
                        Toast.makeText(
                            this@Timeline,
                            "See something you like :)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            this@Timeline,
                            "Something went wrong: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                // Query timesheet entries based on the selected date
                timesheetCollection
                    .whereEqualTo("date", selectedDate)
                    .get()
                    .addOnSuccessListener { documents ->
                        // Process and display timesheet entries for the selected date
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            "Something went wrong: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}


