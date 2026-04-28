package com.budgetbuddy.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.budgetbuddy.R
import com.budgetbuddy.data.DBHelper
import com.budgetbuddy.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DBHelper(this)
        sessionManager = SessionManager(this)

        // Auto-login
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = dbHelper.loginUser(username, password)
            if (userId != -1) {
                sessionManager.saveLoginSession(userId)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dbHelper.registerUser(username, password)) {
                Toast.makeText(this, "Registered! Please login.", Toast.LENGTH_SHORT).show()
                etUsername.text.clear()
                etPassword.text.clear()
            } else {
                Toast.makeText(this, "Username exists", Toast.LENGTH_SHORT).show()
            }
        }
    }
}