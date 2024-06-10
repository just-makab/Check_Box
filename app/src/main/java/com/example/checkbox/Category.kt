package com.example.checkbox

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class Category : AppCompatActivity() {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var addCategoryButton: Button
    private lateinit var searchButton: Button
    private lateinit var textDate: EditText
    private lateinit var timeDisplay: TextView
    private lateinit var dateDisplay: TextView
    private lateinit var spinner: Spinner
    private lateinit var totalTimeTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category)

        dateDisplay = findViewById(R.id.DateDisplay)
        timeDisplay = findViewById(R.id.TimeDisplay)
        updateTime()

        textDate = findViewById(R.id.editTextDate)
        textDate.setOnClickListener {
            showDatePicker()
        }

        fStore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()

        //Input Stuff
        spinner = findViewById(R.id.CategorySpinner)
        populateSpinner()

        totalTimeTextView = findViewById(R.id.totalTime)

        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val selectedCategory = spinner.selectedItem.toString()
            val selectedDate = textDate.text.toString()

            if (selectedCategory == "Select a category") {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            calculateTotalTimeForCategory(selectedCategory)
        }

        bottomNav = findViewById(R.id.bottomNavigationView)

        when (this::class.java) {
            Category::class.java -> bottomNav.menu.findItem(R.id.category).isChecked = true
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
                R.id.notifications -> {
                    startActivity(Intent(this, Notification::class.java))
                    finish()
                    true
                }
                R.id.pomodoroTimer -> {
                    startActivity(Intent(this, PomodoroTimer::class.java))
                    finish()
                    true
                }
                else -> {
                    false
                }
            }
        }

        addCategoryButton = findViewById(R.id.addCategoryButton)
        addCategoryButton.setOnClickListener() {
            val intent = Intent(this, AddCategory::class.java)
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
            // Set date to EditText or TextView
            textDate.setText(String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth))
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun populateSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            fStore.collection("users").document(uid).collection("categories")
                .get()
                .addOnSuccessListener { documents ->
                    val titles = mutableListOf<String>()
                    titles.add("Select a category")
                    for (document in documents) {
                        val title = document.getString("title")
                        title?.let {
                            titles.add(it)
                        }
                    }

                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    // Set the hint text to be non-selectable
                    spinner.setSelection(0, false)
                }
                .addOnFailureListener { exception ->
                    // Show toast message for error
                    Toast.makeText(
                        this,
                        "Error getting categories: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun calculateTotalTimeForCategory(category: String) {
        val userId = fAuth.currentUser?.uid ?: return
        val selectedDate = textDate.text.toString()

        fStore.collection("users").document(userId).collection("timeSheets")
            .whereEqualTo("category", category)
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { documents ->
                var totalMinutes = 0
                documents.forEach { document ->
                    totalMinutes += (document.getLong("totalMinutes") ?: 0L).toInt()
                }
                displayTotalTime(totalMinutes)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching time sheets: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayTotalTime(totalMinutes: Int) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        totalTimeTextView.text = if (totalMinutes > 0) {
            " $hours Hours\n $minutes Minutes"
        } else {
            "Nothing done yet"
        }
    }
}