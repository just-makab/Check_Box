package com.example.checkbox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddCategory : AppCompatActivity() {

    private lateinit var inputCategoryTitle: EditText
    private lateinit var createButton: Button
    private lateinit var backButton: Button
    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        inputCategoryTitle = findViewById(R.id.inputCategoryTitle)
        createButton = findViewById(R.id.CreateButton)
        backButton = findViewById(R.id.BackButton)

        backButton.setOnClickListener {
            val intent = Intent(this,Category::class.java)
            startActivity(intent)
            finish()
        }

        createButton.setOnClickListener {
            val categoryTitle = inputCategoryTitle.text.toString().trim()
            if (categoryTitle.isBlank()) {
                inputCategoryTitle.error = "Title can't be blank"
                inputCategoryTitle.requestFocus()
            } else {
                val currentUser = fAuth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    createCategory(userId, categoryTitle)
                } else {
                    Toast.makeText(this, "How did you get here? Login!.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createCategory(userId: String, categoryTitle: String) {
        val category = hashMapOf(
            "title" to categoryTitle,
            "userId" to userId
        )
        fStore.collection("users").document(userId).collection("categories")
            .add(category)
            .addOnSuccessListener {
                Toast.makeText(this, "$categoryTitle category added successfully :)",
                    Toast.LENGTH_SHORT
                ).show()
                inputCategoryTitle.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding category: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}

