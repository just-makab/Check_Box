package com.example.checkbox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var fAuth: FirebaseAuth
    private lateinit var loginTextBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userEmail = findViewById(R.id.inputUserEmail)
        userPassword = findViewById(R.id.Password)
        loginButton = findViewById(R.id.loginButton)
        loginTextBtn = findViewById(R.id.loginTextView)

        fAuth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            loginUser()
        }

        loginTextBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser() {
        val email = userEmail.text.toString().trim()
        val pass = userPassword.text.toString().trim()

        //Input Validation Handling stuff
        if (email.isBlank()) {
            userEmail.error = "Email can't be blank"
            userEmail.requestFocus()
            return
        }
        if (pass.isBlank()) {
            userPassword.error = "Password can't be blank"
            userPassword.requestFocus()
            return
        }

        //Authentication
        fAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Welcome back :)", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    this, MainActivity::class.java
                )
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login Failed! :(", Toast.LENGTH_SHORT).show()
            }
        }
    }
}