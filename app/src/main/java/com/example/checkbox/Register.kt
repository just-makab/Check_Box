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
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var userName : EditText
    private lateinit var userEmail: EditText
    private lateinit var userPassword : EditText
    private lateinit var confirmedPassword : EditText
    private lateinit var registerButton: Button
    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var loginTextBtn: TextView
    private lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userName = findViewById(R.id.inputUserName)
        userEmail = findViewById(R.id.inputUserEmail)
        userPassword = findViewById(R.id.Password)
        confirmedPassword = findViewById(R.id.PasswordConfirm)
        registerButton = findViewById(R.id.registerButton)
        loginTextBtn = findViewById(R.id.loginTextView)

        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        registerButton.setOnClickListener {
            registerUser()
        }

        loginTextBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val userNameValue = userName.text.toString().trim()
        val email = userEmail.text.toString().trim()
        val pass = userPassword.text.toString().trim()
        val confirmPass = confirmedPassword.text.toString().trim()

        //Input Validation Stuff
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
        if (confirmPass.isBlank()) {
            confirmedPassword.error = "Confirm Password can't be blank"
            confirmedPassword.requestFocus()
            return
        }
        if (pass != confirmPass) {
            confirmedPassword.error = "Passwords do not match"
            confirmedPassword.requestFocus()
            return
        }

            //Firebase authentication and storing
            fAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
                    userID = fAuth.currentUser!!.uid
                    val userMap = hashMapOf(
                        "userName" to userNameValue,
                        "email" to email
                    )

                    fStore.collection("users").document(userID)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User profile created", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_SHORT).show()
                        }

                    val intent = Intent(
                        this, Login::class.java
                    )
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Sign Up Failed!", Toast.LENGTH_SHORT).show()
                }
            }



    }
}