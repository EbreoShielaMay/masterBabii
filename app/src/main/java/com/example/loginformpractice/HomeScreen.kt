package com.example.loginformpractice

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class HomeScreen : AppCompatActivity() {

    private lateinit var userNameDisplay: TextView
    private lateinit var avatarInitial: TextView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("HomeScreen", "onCreate called")

        try {
            setContentView(R.layout.home_screen)
            android.util.Log.d("HomeScreen", "Layout set successfully")
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Error setting layout: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error loading home screen", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize views
        try {
            userNameDisplay = findViewById(R.id.userName)
            avatarInitial = findViewById(R.id.avatarInitial)
            logoutButton = findViewById(R.id.logoutButton)
            android.util.Log.d("HomeScreen", "Views bound successfully")
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Error binding views: ${e.message}")
            e.printStackTrace()
            return
        }

        // Get user data from intent first, fallback to SharedPreferences
        val username = intent.getStringExtra("username") ?: getLoggedInUsername()
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")

        android.util.Log.d("HomeScreen", "Username: $username, Name: $name, Email: $email")

        // Check if user is logged in
        if (username.isNullOrEmpty()) {
            android.util.Log.d("HomeScreen", "No username found, redirecting to login")
            navigateToLogin()
            return
        }

        // Display user data
        if (!name.isNullOrEmpty()) {
            // Use data from intent (faster, no Firebase call needed)
            android.util.Log.d("HomeScreen", "Using name from intent: $name")
            displayUserData(name)
        } else {
            // Load from Firebase if not available in intent
            android.util.Log.d("HomeScreen", "Loading user data from Firebase for: $username")
            loadUserDataFromFirebase(username)
        }

        // Set up logout button
        logoutButton.setOnClickListener {
            android.util.Log.d("HomeScreen", "Logout button clicked")
            logout()
        }
    }

    // Encode email to make it Firebase-safe
    private fun encodeEmail(email: String): String {
        return email.replace(".", ",")
            .replace("@", "_at_")
            .replace("#", "_hash_")
            .replace("$", "_dollar_")
            .replace("[", "_open_")
            .replace("]", "_close_")
    }

    private fun displayUserData(name: String) {
        userNameDisplay.text = name
        avatarInitial.text = name.firstOrNull()?.toString()?.uppercase() ?: "U"
        android.util.Log.d("HomeScreen", "Displayed user data: $name")
    }

    private fun loadUserDataFromFirebase(username: String) {
        val database = Firebase.database

        // Encode the email/username for Firebase
        val encodedKey = encodeEmail(username)
        android.util.Log.d("HomeScreen", "Original: $username, Encoded: $encodedKey")

        val userRef = database.getReference("Users").child(encodedKey)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "User"
                    android.util.Log.d("HomeScreen", "User data loaded from Firebase: $name")
                    displayUserData(name)
                } else {
                    android.util.Log.d("HomeScreen", "User data not found in Firebase")
                    userNameDisplay.text = "User"
                    avatarInitial.text = "U"
                    Toast.makeText(
                        this@HomeScreen,
                        "Could not load user data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("HomeScreen", "Firebase error: ${error.message}")
                userNameDisplay.text = "Error"
                avatarInitial.text = "?"
                Toast.makeText(
                    this@HomeScreen,
                    "Error loading user data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getLoggedInUsername(): String? {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        android.util.Log.d("HomeScreen", "Retrieved username from SharedPreferences: $username")
        return username
    }

    private fun logout() {
        // Clear session data
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPref.edit().apply {
            clear()
            apply()
        }

        android.util.Log.d("HomeScreen", "Session cleared, navigating to login")

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate back to login
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back to login screen
        // User must use logout button
        android.util.Log.d("HomeScreen", "Back button pressed - ignored")
        Toast.makeText(this, "Please use logout button to exit", Toast.LENGTH_SHORT).show()
    }
}