package com.budgetbuddy.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.budgetbuddy.R
import com.budgetbuddy.data.DBHelper
import com.budgetbuddy.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var tvGreeting: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var tvCurrentTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)
        sessionManager = SessionManager(this)

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            logout()
            return
        }

        tvGreeting = findViewById(R.id.tvGreeting)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        tvCurrentTotal = findViewById(R.id.tvCurrentTotal)

        // Using View now because the clickable elements are MaterialCardViews, not Buttons
        val btnAddExpense = findViewById<View>(R.id.btnAddExpense)
        val btnViewExpenses = findViewById<View>(R.id.btnViewExpenses)
        val btnManageCategories = findViewById<View>(R.id.btnManageCategories)
        val btnCategoryReport = findViewById<View>(R.id.btnCategoryReport)
        val btnSetBudget = findViewById<View>(R.id.btnSetBudget)
        val btnLogout = findViewById<View>(R.id.btnLogout)

        btnAddExpense.setOnClickListener { startActivity(Intent(this, AddExpenseActivity::class.java)) }
        btnViewExpenses.setOnClickListener { startActivity(Intent(this, ExpenseListActivity::class.java)) }
        btnManageCategories.setOnClickListener { startActivity(Intent(this, ManageCategoriesActivity::class.java)) }
        btnCategoryReport.setOnClickListener { startActivity(Intent(this, CategoryReportActivity::class.java)) }
        btnSetBudget.setOnClickListener { showSetBudgetDialog() }
        btnLogout.setOnClickListener { logout() }

        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val userId = sessionManager.getUserId()
        val cal = Calendar.getInstance()
        val yearMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
        val total = dbHelper.getTotalExpensesForCurrentMonth(userId, yearMonth)
        tvCurrentTotal.text = "This month: R$total"

        val minGoal = sessionManager.getMonthlyMinGoal(userId)
        val maxGoal = sessionManager.getMonthlyMaxGoal(userId)

        val status = if (minGoal > 0 && maxGoal > 0) {
            when {
                total < minGoal -> "⚠️ Below minimum goal (₹$minGoal)"
                total > maxGoal -> "⚠️ Exceeded max budget (₹$maxGoal)"
                else -> "✅ Within budget (₹$minGoal - ₹$maxGoal)"
            }
        } else {
            "Set budget goals (Min & Max)"
        }
        tvBudgetStatus.text = status

        val username = "User"
        tvGreeting.text = "Hello, ${getUsernameFromDb(userId)}"
    }

    private fun getUsernameFromDb(userId: Int): String {
        return "Budgeter"
    }

    private fun showSetBudgetDialog() {
        val userId = sessionManager.getUserId()
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_budget, null)
        val etMin = view.findViewById<EditText>(R.id.etMinGoal)
        val etMax = view.findViewById<EditText>(R.id.etMaxGoal)
        etMin.setText(sessionManager.getMonthlyMinGoal(userId).toString())
        etMax.setText(sessionManager.getMonthlyMaxGoal(userId).toString())

        builder.setTitle("Set Monthly Budget Goals")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val min = etMin.text.toString().toDoubleOrNull() ?: 0.0
                val max = etMax.text.toString().toDoubleOrNull() ?: 0.0
                if (min >= 0 && max >= min) {
                    sessionManager.setMonthlyMinGoal(userId, min)
                    sessionManager.setMonthlyMaxGoal(userId, max)
                    updateDashboard()
                    Toast.makeText(this, "Budget goals saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Min ≥ 0 and Max ≥ Min", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        sessionManager.clearSession()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}