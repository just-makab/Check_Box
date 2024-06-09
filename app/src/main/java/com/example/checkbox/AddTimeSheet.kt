package com.example.checkbox

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale


class AddTimeSheet : AppCompatActivity() {

    private lateinit var fStore : FirebaseFirestore
    private lateinit var fAuth: FirebaseAuth

    private lateinit var spinner: Spinner
    private lateinit var taskTitle : EditText
    private lateinit var description : EditText
    private lateinit var textDate : EditText
    private lateinit var textStartTime: EditText
    private lateinit var textEndTime : EditText
    private lateinit var imagePreView : ImageView
    private var selectedImageUri: Uri? = null
    private val requestImageGallery = 9137 //IMG :)
    private lateinit var captureButton: Button
    private lateinit var saveButton: Button
    private lateinit var backButton: Button


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_time_sheet)

        fStore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()

        //Input Stuff
        spinner = findViewById(R.id.CategorySpinner)
        taskTitle = findViewById(R.id.TaskTitle)
        description = findViewById(R.id.Description)

        //Date and time stuff
        textDate = findViewById(R.id.editTextDate)
        textEndTime = findViewById(R.id.editTextEndTime)
        textStartTime = findViewById(R.id.editTextStartTime)

        populateSpinner()

        //Date and time stuff
        textDate.setOnClickListener {
            showDatePicker()
        }

        textStartTime.setOnClickListener {
            showTimePicker(true)
        }

        textEndTime.setOnClickListener {
            showTimePicker(false)
        }

        //camera stuff
        imagePreView = findViewById(R.id.imagePreview)
        captureButton = findViewById(R.id.CaptureButton)
        captureButton.setOnClickListener {
            openGallery()
            Toast.makeText(this, "Please select ONE IMAGE", Toast.LENGTH_SHORT).show()
        }


        saveButton = findViewById(R.id.saveButton)
        saveButton.setOnClickListener{
            validateAndSave()
        }

        backButton = findViewById(R.id.BackButton)
        backButton.setOnClickListener {
            val intent = Intent(this, Timeline::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Camera stuff
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, requestImageGallery)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestImageGallery && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            imagePreView.setImageURI(selectedImageUri)
        }
    }


    //Date stuff
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { view, year, monthOfYear, dayOfMonth ->
            // Set date to EditText or TextView
            textDate.setText(String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth))
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // TimePickerDialog
        val timePickerDialog = TimePickerDialog(this, { view, hourOfDay, minute ->
            // Format the time
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            if (isStartTime) {
                textStartTime.setText(formattedTime)
            } else {
                textEndTime.setText(formattedTime)
            }
        }, hour, minute, true)

        timePickerDialog.show()
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

    //Storage stuff
    private fun validateAndSave() {
        saveButton.isEnabled = false
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (spinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_LONG).show()
            return
        }

        val title = taskTitle.text.toString().trim()
        if (title.isEmpty()) {
            taskTitle.error = "Title cannot be blank"
            taskTitle.requestFocus()
            return
        }

        val descriptionText = description.text.toString().trim()
        if (descriptionText.isEmpty()) {
            description.error = "Description cannot be blank"
            description.requestFocus()
            return
        }

        if (textDate.text.toString().isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_LONG).show()
            return
        }

        if (textStartTime.text.toString().isEmpty()) {
            Toast.makeText(this, "Please select a start time", Toast.LENGTH_LONG).show()
            return
        }

        if (textEndTime.text.toString().isEmpty()) {
            Toast.makeText(this, "Please select an end time", Toast.LENGTH_LONG).show()
            return
        }

        val totalMinutes = calculateDifference()
        val selectedCategory = spinner.selectedItem.toString()

        if (selectedImageUri == null) {
            selectedImageUri = getDefaultImageUri()
        }

        val timeSheetEntry = hashMapOf(
            "userId" to userId,
            "category" to selectedCategory,
            "taskTitle" to title,
            "description" to descriptionText,
            "date" to textDate.text.toString(),
            "startTime" to textStartTime.text.toString(),
            "endTime" to textEndTime.text.toString(),
            "photoData" to selectedImageUri.toString(),
            "totalMinutes" to totalMinutes
        )

        userId?.let { uid ->
            val timesheetCollection = fStore.collection("users").document(uid).collection("timeSheets")
            timesheetCollection
            .add(timeSheetEntry)
            .addOnSuccessListener {
                val hoursWorked = totalMinutes / 60
                Toast.makeText(this, "Well done on completing $hoursWorked hours :)", Toast.LENGTH_LONG).show()
                saveButton.isEnabled = true
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "something went wrong :( $exception", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
            }
        }

    }

    private fun calculateDifference(): Int {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTime = timeFormat.parse(textStartTime.text.toString())
        val endTime = timeFormat.parse(textEndTime.text.toString())

        if (startTime != null && endTime != null) {
            val calStart = Calendar.getInstance().apply { time = startTime }
            val calEnd = Calendar.getInstance().apply { time = endTime }

            // Check if end time is before start time (indicating end time is on the next day)
            if (calEnd.before(calStart)) {
                Toast.makeText(this, "End time cannot be before start time. Please enter time spent on the same day and create a new entry for the next day.", Toast.LENGTH_LONG).show()
                return 0 // Return 0 if there is an error
            }

            val difference = calEnd.timeInMillis - calStart.timeInMillis
            return (difference / (1000 * 60)).toInt() // Return the total minutes as an integer
        }
        return 0
    }


    private fun getDefaultImageUri(): Uri {
        return Uri.parse("android.resource://$packageName/drawable/no_image")
    }

}

