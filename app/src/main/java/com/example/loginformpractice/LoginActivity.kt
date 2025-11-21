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

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        // Bind views
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.user_login_button)


        // Login button listener
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString()

            // Clear previous errors
            usernameInput.error = null
            passwordInput.error = null

            // Validate inputs
            when {
                username.isEmpty() -> {
                    usernameInput.error = "Username is required"
                    usernameInput.requestFocus()
                }
                password.isEmpty() -> {
                    passwordInput.error = "Password is required"
                    passwordInput.requestFocus()
                }
                else -> {
                    // Disable button to prevent multiple clicks
                    loginButton.isEnabled = false
                    loginUser(username, password, loginButton)
                }
            }
        }
    }

    private fun loginUser(username: String, password: String, button: Button) {
        val database = Firebase.database
        val usersRef = database.getReference("Users")

        // Fetch user data from database
        usersRef.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User exists, check password
                    val storedPassword = snapshot.child("password").getValue(String::class.java)
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    if (storedPassword == password) {
                        // Password matches - login successful
                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome back, $name!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Save login session (optional - using SharedPreferences)
                        saveLoginSession(username, name, email)

                        // Navigate to main activity
                        Intent(this@LoginActivity, MainActivity::class.java).also {
                            it.putExtra("username", username)
                            it.putExtra("name", name)
                            it.putExtra("email", email)
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                        finish()
                    } else {
                        // Password doesn't match
                        Toast.makeText(
                            this@LoginActivity,
                            "Incorrect password",
                            Toast.LENGTH_SHORT
                        ).show()
                        button.isEnabled = true
                    }
                } else {
                    // User doesn't exist
                    Toast.makeText(
                        this@LoginActivity,
                        "Username not found. Please register first.",
                        Toast.LENGTH_SHORT
                    ).show()
                    button.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                button.isEnabled = true
            }
        })
    }

    private fun saveLoginSession(username: String, name: String?, email: String?) {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("username", username)
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already logged in
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val username = sharedPref.getString("username", "")
            val name = sharedPref.getString("name", "")
            val email = sharedPref.getString("email", "")

            // User is already logged in, navigate to main activity
            Intent(this, HomeScreen::class.java).also {
                it.putExtra("username", username)
                it.putExtra("name", name)
                it.putExtra("email", email)
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
            finish()
        }
    }
}