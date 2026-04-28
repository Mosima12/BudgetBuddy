package com.budgetbuddy.ui

import android.app.DatePickerDialog
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.R
import com.budgetbuddy.adapter.ExpenseAdapter
import com.budgetbuddy.data.DBHelper
import java.util.*

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var btnFilter: Button
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private var startDate = ""
    private var endDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        dbHelper = DBHelper(this)
        recyclerView = findViewById(R.id.rvExpenses)
        btnFilter = findViewById(R.id.btnFilter)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(this, mutableListOf())
        recyclerView.adapter = adapter

        tvStartDate.setOnClickListener { showDatePicker(true) }
        tvEndDate.setOnClickListener { showDatePicker(false) }
        btnFilter.setOnClickListener { loadExpenses() }

        loadExpenses()
    }

    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _: DatePicker, year: Int, month: Int, day: Int ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, day)
            if (isStart) {
                startDate = date
                tvStartDate.text = date
            } else {
                endDate = date
                tvEndDate.text = date
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadExpenses() {
        val cursor: Cursor = if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            dbHelper.getExpensesBetweenDates(startDate, endDate)
        } else {
            dbHelper.getAllExpenses()
        }
        val expenses = mutableListOf<ExpenseAdapter.ExpenseItem>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_EXPENSE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TITLE))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_AMOUNT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_DATE))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_IMAGE_PATH))
            expenses.add(ExpenseAdapter.ExpenseItem(id, title, amount, date, category, imagePath))
        }
        cursor.close()
        adapter.updateList(expenses)
    }
}