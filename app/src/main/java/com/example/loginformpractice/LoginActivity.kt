package com.example.loginformpractice

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        android.util.Log.d("LoginActivity", "onCreate started")

        // Bind views
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.user_login_button)

        android.util.Log.d("LoginActivity", "Views bound successfully")

        // Login button listener
        loginButton.setOnClickListener {
            android.util.Log.d("LoginActivity", "Login button clicked")

            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString()

            android.util.Log.d("LoginActivity", "Email: $email, Password length: ${password.length}")

            // Clear previous errors
            usernameInput.error = null
            passwordInput.error = null

            // Validate inputs
            when {
                email.isEmpty() -> {
                    usernameInput.error = "Email is required"
                    usernameInput.requestFocus()
                    android.util.Log.d("LoginActivity", "Validation failed: Email empty")
                }
                password.isEmpty() -> {
                    passwordInput.error = "Password is required"
                    passwordInput.requestFocus()
                    android.util.Log.d("LoginActivity", "Validation failed: Password empty")
                }
                else -> {
                    // Disable button to prevent multiple clicks
                    loginButton.isEnabled = false
                    android.util.Log.d("LoginActivity", "Validation passed, calling loginUser")
                    loginUser(email, password, loginButton)
                }
            }
        }
    }

    private fun loginUser(email: String, password: String, button: Button) {
        val database = Firebase.database
        val usersRef = database.getReference("Users")

        android.util.Log.d("LoginActivity", "Attempting to login with email: $email")

        // Query users by email field instead of using email as path key
        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    android.util.Log.d("LoginActivity", "Database response received")

                    if (snapshot.exists()) {
                        // User with this email exists
                        // Get the first (and should be only) matching user
                        val userSnapshot = snapshot.children.first()

                        val storedPassword = userSnapshot.child("password").getValue(String::class.java)
                        val username = userSnapshot.child("username").getValue(String::class.java)
                        val userEmail = userSnapshot.child("email").getValue(String::class.java)

                        android.util.Log.d("LoginActivity", "User found: $username")

                        if (storedPassword == password) {
                            // Password matches - login successful
                            android.util.Log.d("LoginActivity", "Password correct, navigating to HomeScreen")

                            Toast.makeText(
                                this@LoginActivity,
                                "Welcome back, $username!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Save login session
                            saveLoginSession(username, userEmail)

                            // Navigate to HomeScreen
                            try {
                                val intent = Intent(this@LoginActivity, HomeScreen::class.java)
                                intent.putExtra("username", username)
                                intent.putExtra("name", username)
                                intent.putExtra("email", userEmail)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                                android.util.Log.d("LoginActivity", "Starting HomeScreen activity")
                                startActivity(intent)

                                android.util.Log.d("LoginActivity", "Finishing LoginActivity")
                                finish()
                            } catch (e: Exception) {
                                android.util.Log.e("LoginActivity", "Error starting HomeScreen: ${e.message}")
                                e.printStackTrace()
                                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                button.isEnabled = true
                            }
                        } else {
                            // Password doesn't match
                            android.util.Log.d("LoginActivity", "Password incorrect")
                            Toast.makeText(
                                this@LoginActivity,
                                "Incorrect password",
                                Toast.LENGTH_SHORT
                            ).show()
                            button.isEnabled = true
                        }
                    } else {
                        // User doesn't exist
                        android.util.Log.d("LoginActivity", "User not found")
                        Toast.makeText(
                            this@LoginActivity,
                            "Email not found. Please register first.",
                            Toast.LENGTH_SHORT
                        ).show()
                        button.isEnabled = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("LoginActivity", "Database error: ${error.message}")
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    button.isEnabled = true
                }
            })
    }

    private fun saveLoginSession(username: String?, email: String?) {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("username", username)
        editor.putString("name", username)  // Using username as display name
        editor.putString("email", email)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

        android.util.Log.d("LoginActivity", "Session saved for user: $username")
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already logged in
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        android.util.Log.d("LoginActivity", "onStart - isLoggedIn: $isLoggedIn")

        if (isLoggedIn) {
            val username = sharedPref.getString("username", "")
            val name = sharedPref.getString("name", "")
            val email = sharedPref.getString("email", "")

            android.util.Log.d("LoginActivity", "Auto-login for user: $username")

            // User is already logged in, navigate to HomeScreen
            try {
                val intent = Intent(this, HomeScreen::class.java)
                intent.putExtra("username", username)
                intent.putExtra("name", name)
                intent.putExtra("email", email)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Error in auto-login: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}