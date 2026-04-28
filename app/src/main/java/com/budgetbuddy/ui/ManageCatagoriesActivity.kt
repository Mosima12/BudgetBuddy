package com.budgetbuddy.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.budgetbuddy.R
import com.budgetbuddy.data.DBHelper

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val categories = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        dbHelper = DBHelper(this)
        listView = findViewById(R.id.lvCategories)
        val btnAdd = findViewById<Button>(R.id.btnAddCategory)
        loadCategories()

        btnAdd.setOnClickListener { showAddCategoryDialog() }
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(dbHelper.getAllCategories().map { it.second })
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        listView.adapter = adapter
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (dbHelper.addCategory(name)) {
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Category may already exist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}