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
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import android.graphics.Color
import com.github.mikephil.charting.data.BarData
import android.view.View
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var timeDisplay: TextView
    private lateinit var dateDisplay: TextView
    private lateinit var minGoalText: EditText
    private lateinit var maxGoalText: EditText
    private lateinit var saveGoal: Button
    private lateinit var chart: HorizontalBarChart
    private lateinit var lineChart: LineChart
    private lateinit var displayChartButton: Button
    private lateinit var displayLineChartButton: Button
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var fStore: FirebaseFirestore
    private lateinit var fAuth: FirebaseAuth
    private var minGoal: Int = 0
    private var maxGoal: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        fStore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()

        chart = findViewById(R.id.chart)
        lineChart = findViewById(R.id.lineChart)
        displayChartButton = findViewById(R.id.displayChartButton)
        displayLineChartButton = findViewById(R.id.displayLineChartButton)
        startDateEditText = findViewById(R.id.startDate)
        endDateEditText = findViewById(R.id.endDate)

        displayChartButton.setOnClickListener {
            fetchUserGoals()
        }

        displayLineChartButton.setOnClickListener {
            fetchLineChartData()
        }

        startDateEditText.setOnClickListener {
            showDatePickerDialog(startDateEditText)
        }

        endDateEditText.setOnClickListener {
            showDatePickerDialog(endDateEditText)
        }

        minGoalText = findViewById(R.id.MinGoal)
        maxGoalText = findViewById(R.id.MaxGoal)
        saveGoal = findViewById(R.id.SaveGoalBtn)

        saveGoal.setOnClickListener {
            val minGoalValue = minGoalText.text.toString()
            val maxGoalValue = maxGoalText.text.toString()

            if (minGoalValue.isEmpty()) {
                minGoalText.error = "Minimum goal cannot be empty"
                minGoalText.requestFocus()
                return@setOnClickListener
            }

            if (maxGoalValue.isEmpty()) {
                maxGoalText.error = "Maximum goal cannot be empty"
                maxGoalText.requestFocus()
                return@setOnClickListener
            }

            val minGoalInt = minGoalValue.toIntOrNull()
            val maxGoalInt = maxGoalValue.toIntOrNull()

            if (minGoalInt == null || maxGoalInt == null) {
                Toast.makeText(this, "Goals must be valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minGoalInt >= maxGoalInt) {
                maxGoalText.error = "Maximum goal must be greater than minimum goal"
                maxGoalText.requestFocus()
                return@setOnClickListener
            }

            if (minGoalInt < 1) {
                minGoalText.error = "Minimum goal must be at least 1 hour"
                minGoalText.requestFocus()
                return@setOnClickListener
            }

            if (maxGoalInt > 10) {
                maxGoalText.error = "Maximum goal cannot exceed 10 hours"
                maxGoalText.requestFocus()
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

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            editText.setText(date)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun fetchUserGoals() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            fStore.collection("users").document(uid).collection("goals").get()
                .addOnSuccessListener { documents ->
                    if (documents != null && documents.documents.isNotEmpty()) {
                        val document = documents.documents[0]
                        minGoal = (document.getLong("minGoal") ?: 0).toInt()
                        maxGoal = (document.getLong("maxGoal") ?: 0).toInt()
                        fetchMonthlyData()
                    } else {
                        Toast.makeText(this, "No goals set. Please set your goals first.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->

                    Toast.makeText(this, "Error fetching goals: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun fetchMonthlyData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val last30Days = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        userId?.let { uid ->
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            userRef.collection("dailyTotals")
                .whereGreaterThan("date", dateFormat.format(last30Days.time))
                .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val dateLabels = ArrayList<String>()
                    val totalMinutesData = ArrayList<BarEntry>()

                    documents.forEachIndexed { index, document ->
                        val totalMinutes = document.getLong("totalMinutes") ?: 0
                        val totalHours = totalMinutes.toFloat() / 60 // Convert minutes to hours
                        dateLabels.add(document.id)
                        totalMinutesData.add(BarEntry(index.toFloat(), totalHours))
                    }

                    updateBarChart(dateLabels, totalMinutesData)
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                    Toast.makeText(this, "Error fetching data: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun fetchLineChartData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val startDate = startDateEditText.text.toString()
        val endDate = endDateEditText.text.toString()

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select both start and end dates.", Toast.LENGTH_LONG).show()
            return
        }

        userId?.let { uid ->
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            userRef.collection("dailyTotals")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val dateLabels = ArrayList<String>()
                    val totalMinutesData = ArrayList<Entry>()

                    documents.forEachIndexed { index, document ->
                        val totalMinutes = document.getLong("totalMinutes") ?: 0
                        val totalHours = totalMinutes.toFloat() / 60 // Convert minutes to hours
                        dateLabels.add(document.id)
                        totalMinutesData.add(Entry(index.toFloat(), totalHours))
                    }

                    updateLineChart(dateLabels, totalMinutesData)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching data: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updateBarChart(labels: List<String>, data: List<BarEntry>) {
        val dataSet = BarDataSet(data, "Total Hours")
        dataSet.colors = data.map { entry ->
            val totalHours = entry.y.toInt()
            when {
                totalHours < minGoal -> Color.BLUE
                totalHours > maxGoal -> Color.RED
                else -> Color.GREEN
            }
        }

        val barData = BarData(dataSet)
        chart.data = barData
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        val legend = chart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.SQUARE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.orientation = Legend.LegendOrientation.HORIZONTAL

        val legendEntries = arrayOf(
            LegendEntry("Below Minimum Goal", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.BLUE),
            LegendEntry("Within Goal", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.GREEN),
            LegendEntry("Above Maximum Goal", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.RED)
        )
        legend.setCustom(legendEntries)

        chart.invalidate()

        chart.visibility = View.VISIBLE // Make chart visible
    }

    private fun updateLineChart(labels: List<String>, data: List<Entry>) {
        val dataSet = LineDataSet(data, "Total Hours")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.invalidate() // refresh chart

        lineChart.visibility = View.VISIBLE // Make chart visible
    }
}
