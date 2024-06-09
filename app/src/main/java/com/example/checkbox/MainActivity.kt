package com.example.checkbox

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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var timeDisplay: TextView
    private lateinit var dateDisplay: TextView
    private lateinit var minGoal: EditText
    private lateinit var maxGoal: EditText
    private lateinit var saveGoal: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        minGoal = findViewById(R.id.MinGoal)
        maxGoal = findViewById(R.id.MaxGoal)
        saveGoal = findViewById(R.id.SaveGoalBtn)

        saveGoal.setOnClickListener {
            val minGoalValue = minGoal.text.toString()
            val maxGoalValue = maxGoal.text.toString()

            if (minGoalValue.isEmpty()) {
                minGoal.error = "Minimum goal cannot be empty"
                minGoal.requestFocus()
                return@setOnClickListener
            }

            if (maxGoalValue.isEmpty()) {
                maxGoal.error = "Maximum goal cannot be empty"
                maxGoal.requestFocus()
                return@setOnClickListener
            }

            val minGoalInt = minGoalValue.toIntOrNull()
            val maxGoalInt = maxGoalValue.toIntOrNull()

            if (minGoalInt == null || maxGoalInt == null) {
                Toast.makeText(this, "Goals must be valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minGoalInt >= maxGoalInt) {
                maxGoal.error = "Maximum goal must be greater than minimum goal"
                maxGoal.requestFocus()
                return@setOnClickListener
            }

            if (minGoalInt < 1) {
                minGoal.error = "Minimum goal must be at least 1 hour"
                minGoal.requestFocus()
                return@setOnClickListener
            }

            if (maxGoalInt > 10) {
                maxGoal.error = "Maximum goal cannot exceed 10 hours"
                maxGoal.requestFocus()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let { uid ->
                val db = FirebaseFirestore.getInstance()
                val userGoalsRef = db.collection("users").document(uid).collection("goals")

                val goalData = hashMapOf(
                    "userId" to uid,
                    "minGoal" to minGoalInt,
                    "maxGoal" to maxGoalInt
                ) as Map<String, Any>

                userGoalsRef.get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.isEmpty) {
                            userGoalsRef.add(goalData)
                                .addOnSuccessListener {
                                    runOnUiThread {
                                        Toast.makeText(this, "Goal information saved successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    runOnUiThread {
                                        Toast.makeText(this, "Failed to save goal information", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            val goalDocument = snapshot.documents[0]
                            val goalId = goalDocument.id
                            val docRef = userGoalsRef.document(goalId)
                            docRef.update(goalData)
                                .addOnSuccessListener {
                                    runOnUiThread {
                                        Toast.makeText(this, "Goal information updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    runOnUiThread {
                                        Toast.makeText(this, "Failed to update goal information", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                    .addOnFailureListener {
                        runOnUiThread {
                            Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        dateDisplay = findViewById(R.id.DateDisplay)
        timeDisplay = findViewById(R.id.TimeDisplay)
        updateTime()

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    updateTime()
                }
            }
        }, 0, 60000)

        bottomNav = findViewById(R.id.bottomNavigationView)
        when (this::class.java) {
            MainActivity::class.java -> bottomNav.menu.findItem(R.id.home).isChecked = true
        }
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.category -> {
                    startActivity(Intent(this, Category::class.java))
                    finish()
                    true
                }
                R.id.timeline -> {
                    startActivity(Intent(this, Timeline::class.java))
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
                else -> false
            }
        }
    }

    private fun updateTime() {
        val currentTime = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(currentTime.time)
        timeDisplay.text = formattedTime
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentTime.time)
        dateDisplay.text = formattedDate
    }
}
