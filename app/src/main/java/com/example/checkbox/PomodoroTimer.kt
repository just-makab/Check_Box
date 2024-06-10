package com.example.checkbox

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PomodoroTimer : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var timeSelected: Int = 0
    private var breakCount: Int = 0
    private var timeCountDown: CountDownTimer? = null
    private var timeProgress = 0
    private var pauseOffset: Long = 0
    private var isStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pomodoro_timer)

        val addBtn: ImageButton = findViewById(R.id.btnAdd)
        addBtn.setOnClickListener {
            setTimeFunction()
        }
        val startBtn: Button = findViewById(R.id.btnPlayPause)
        startBtn.setOnClickListener {
            startTimerSetup()
        }

        val resetBtn: ImageButton = findViewById(R.id.ib_reset)
        resetBtn.setOnClickListener {
            resetTime()
        }

        val addTimeTv: TextView = findViewById(R.id.tv_addTime)
        addTimeTv.setOnClickListener {
            addExtraTime()
        }

        bottomNav = findViewById(R.id.bottomNavigationView)
        when (this::class.java) {
            PomodoroTimer::class.java -> bottomNav.menu.findItem(R.id.pomodoroTimer).isChecked = true
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

    private fun addExtraTime() {
        val progressBar: ProgressBar = findViewById(R.id.pbTimer)
        if (timeSelected != 0) {
            timeSelected += 15
            progressBar.max = timeSelected
            timePause()
            startTimer(pauseOffset)
            Toast.makeText(this, "15 sec added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetTime() {
        if (timeCountDown != null) {
            timeCountDown!!.cancel()
            timeProgress = 0
            timeSelected = 0
            pauseOffset = 0
            timeCountDown = null
            val startBtn: Button = findViewById(R.id.btnPlayPause)
            startBtn.text = "Start"
            isStart = true
            val progressBar = findViewById<ProgressBar>(R.id.pbTimer)
            progressBar.progress = 0
            val timeLeftTv: TextView = findViewById(R.id.tvTimeLeft)
            timeLeftTv.text = "0"
        }
    }

    private fun timePause() {
        if (timeCountDown != null) {
            timeCountDown!!.cancel()
        }
    }

    private fun startTimerSetup() {
        val startBtn: Button = findViewById(R.id.btnPlayPause)
        if (timeSelected > timeProgress) {
            if (isStart) {
                startBtn.text = "Pause"
                startTimer(pauseOffset)
                isStart = false
            } else {
                isStart = true
                startBtn.text = "Resume"
                timePause()
            }
        } else {
            Toast.makeText(this, "Enter Time", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer(pauseOffsetL: Long) {
        val progressBar = findViewById<ProgressBar>(R.id.pbTimer)
        progressBar.progress = timeProgress
        val totalTime = timeSelected * 1000
        val remainingTime = totalTime - pauseOffsetL

        timeCountDown = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeProgress++
                pauseOffset = totalTime - millisUntilFinished
                progressBar.progress = timeSelected - timeProgress
                val timeLeftTv: TextView = findViewById(R.id.tvTimeLeft)
                timeLeftTv.text = ((totalTime - pauseOffset) / 1000).toString()

                // Calculate break times
                val remainingTime = (totalTime - pauseOffset) / 1000
                val breakDuration = remainingTime / (breakCount + 1)

                // Schedule breaks
                for (i in 1..breakCount) {
                    if (timeProgress.toLong() == i * breakDuration) {
                        showToast("Time for a break!")
                        // Pause the timer
                        timePause()
                    }
                }
            }

            override fun onFinish() {
                resetTime()
                showToast("Times Up!")
            }
        }.start()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setTimeFunction() {
        val timeDialog = Dialog(this)
        timeDialog.setContentView(R.layout.add_dialog)
        val timeSet = timeDialog.findViewById<EditText>(R.id.etGetTime)
        val breakSet = timeDialog.findViewById<EditText>(R.id.etBreakCount)
        val timeLeftTv: TextView = findViewById(R.id.tvTimeLeft)
        val btnStart: Button = findViewById(R.id.btnPlayPause)
        val progressBar = findViewById<ProgressBar>(R.id.pbTimer)
        timeDialog.findViewById<Button>(R.id.btnOK).setOnClickListener {
            if (timeSet.text.isEmpty() || breakSet.text.isEmpty()) {
                Toast.makeText(this, "Enter Time Duration and Break Count", Toast.LENGTH_SHORT).show()
            } else {
                resetTime()
                timeLeftTv.text = timeSet.text
                btnStart.text = "Start"
                timeSelected = timeSet.text.toString().toInt()
                progressBar.max = timeSelected
                breakCount = breakSet.text.toString().toInt()
            }
            timeDialog.dismiss()
        }
        timeDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timeCountDown?.cancel()
    }
}
