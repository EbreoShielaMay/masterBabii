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


class RegisterActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_main)

        //bindview
        val emailInput = findViewById<EditText>(R.id.email)
        val usernameInput = findViewById<EditText>(R.id.username)
        val passwordInput = findViewById<EditText>(R.id.password)
        val confirmationInput = findViewById<EditText>(R.id.confirmation)
        val button = findViewById<Button>(R.id.user_register_button)

        //listener
        button.setOnClickListener {
            //get text
            val email = emailInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmation = confirmationInput.text.toString()

            //clear previous errors
            emailInput.error = null
            usernameInput.error = null
            passwordInput.error = null
            confirmationInput.error = null

            //validate inputs
            when {
                email.isEmpty() -> {
                    emailInput.error = "Email is required"
                    emailInput.requestFocus()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Please enter a valid email"
                    emailInput.requestFocus()
                }
                username.isEmpty() -> {
                    usernameInput.error = "Username is required"
                    usernameInput.requestFocus()
                }
                password.isEmpty() -> {
                    passwordInput.error = "Password is required"
                    passwordInput.requestFocus()
                }
                password.length < 6 -> {
                    passwordInput.error = "Password must be at least 6 characters"
                    passwordInput.requestFocus()
                }
                confirmation.isEmpty() -> {
                    confirmationInput.error = "Please confirm your password"
                    confirmationInput.requestFocus()
                }
                password != confirmation -> {
                    confirmationInput.error = "Passwords do not match"
                    confirmationInput.requestFocus()
                }
                else -> {
                    //disable button to prevent multiple clicks
                    button.isEnabled = false
                    //check if username exists then add user
                    checkUsernameAndAddUser(email, username, password, button)
                }
            }
        }
    }

    private fun checkUsernameAndAddUser(
        email: String,
        username: String,
        password: String,
        button: Button
    ) {
        val database = Firebase.database
        val usersRef = database.getReference("Users")

        //check if username already exists
        usersRef.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    //username already taken
                    Toast.makeText(
                        this@RegisterActivity,
                        "Username already taken",
                        Toast.LENGTH_SHORT
                    ).show()
                    button.isEnabled = true
                } else {
                    //username available, proceed with registration
                    addUser( email, username, password, button)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                button.isEnabled = true
            }
        })
    }

    private fun addUser(
        email: String,
        username: String,
        password: String,
        button: Button
    ) {
        //create hashmap
        val user = hashMapOf(
            "email" to email,
            "username" to username,
            "password" to password
        )

        //instantiate database connection
        val database = Firebase.database
        val myRef = database.getReference("Users")

        //add to database
        myRef.child(username).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(
                    this@RegisterActivity,
                    "Registration successful!",
                    Toast.LENGTH_SHORT
                ).show()

                //navigate to login or main activity
                finish() // or navigate to another activity
                // Intent(this, LoginActivity::class.java).also {
                //     startActivity(it)
                // }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@RegisterActivity,
                    "Registration failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                button.isEnabled = true
            }
    }
}