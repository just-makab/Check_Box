package com.example.checkbox

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.checkbox.databinding.ActivityNotificationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class Notification : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var categorySpinner: Spinner
    private lateinit var dateDisplay: TextView
    private lateinit var timeDisplay: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dateDisplay = findViewById(R.id.DateDisplay)
        timeDisplay = findViewById(R.id.TimeDisplay)
        updateTime()

        // Update time every minute
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    updateTime()
                }
            }
        }, 0, 60000)

        categorySpinner = findViewById(R.id.categorySpinner)
        populateSpinner()

        createNotificationChannel()
        binding.submitButton.setOnClickListener { scheduleNotification() }

        bottomNav = findViewById(R.id.bottomNavigationView)
        when (this::class.java) {
            Notification::class.java -> bottomNav.menu.findItem(R.id.notifications).isChecked = true
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

    private fun scheduleNotification() {
        val intent = Intent(applicationContext, Notifications::class.java)
        val title = categorySpinner.selectedItem.toString()
        val message = binding.messageET.text.toString()
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime()

        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
                showAlert(time, title, message)
            } else {
                showAlertNoPermission()
            }
        } catch (e: SecurityException) {
            showAlertNoPermission()
        }
    }

    private fun showAlertNoPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Your app does not have the permission to schedule exact alarms. Please grant the permission in the app settings.")
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = DateFormat.getTimeFormat(applicationContext)

        AlertDialog.Builder(this)
            .setTitle("Notification Scheduled")
            .setMessage(
                "Title: $title\nMessage: $message\nAt: ${dateFormat.format(date)} ${timeFormat.format(date)}"
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun getTime(): Long {
        val minute = binding.timePicker.minute
        val hour = binding.timePicker.hour
        val day = binding.datePicker.dayOfMonth
        val month = binding.datePicker.month
        val year = binding.datePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    private fun createNotificationChannel() {
        val name = "Schedule Channel"
        val desc = "Schedule Notifications to complete tasks"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun populateSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            db.collection("users").document(uid).collection("categories")
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
                    categorySpinner.adapter = adapter

                    categorySpinner.setSelection(0, false)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Error getting categories: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
