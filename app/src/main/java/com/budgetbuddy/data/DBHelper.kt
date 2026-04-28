package com.budgetbuddy.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "budgetbuddy.db"
        private const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_USERS = "users"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_EXPENSES = "expenses"

        // Users columns
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"

        // Categories columns
        const val COL_CATEGORY_ID = "id"
        const val COL_CATEGORY_NAME = "name"

        // Expenses columns
        const val COL_EXPENSE_ID = "id"
        const val COL_TITLE = "title"
        const val COL_AMOUNT = "amount"
        const val COL_DATE = "date"
        const val COL_CATEGORY_ID_FK = "category_id"
        const val COL_IMAGE_PATH = "image_path"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL
            )
        """)

        // Categories table
        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORY_NAME TEXT UNIQUE NOT NULL
            )
        """)

        // Expenses table
        db.execSQL("""
            CREATE TABLE $TABLE_EXPENSES (
                $COL_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_AMOUNT REAL NOT NULL,
                $COL_DATE TEXT NOT NULL,
                $COL_CATEGORY_ID_FK INTEGER NOT NULL,
                $COL_IMAGE_PATH TEXT,
                FOREIGN KEY ($COL_CATEGORY_ID_FK) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID) ON DELETE CASCADE
            )
        """)

        // Insert default categories
        insertDefaultCategories(db)
        // Insert a demo user for convenience
        insertDemoUser(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        val categories = listOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Health")
        categories.forEach { name ->
            val cv = ContentValues().apply { put(COL_CATEGORY_NAME, name) }
            db.insert(TABLE_CATEGORIES, null, cv)
        }
    }

    private fun insertDemoUser(db: SQLiteDatabase) {
        val cv = ContentValues().apply {
            put(COL_USERNAME, "buddy")
            put(COL_PASSWORD, "buddy123")
        }
        db.insert(TABLE_USERS, null, cv)
    }

    // ------------------- User operations -------------------
    fun registerUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, cv) != -1L
    }

    fun loginUser(username: String, password: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS, arrayOf(COL_USER_ID),
            "$COL_USERNAME = ? AND $COL_PASSWORD = ?",
            arrayOf(username, password), null, null, null
        )
        val userId = if (cursor.moveToFirst()) cursor.getInt(0) else -1
        cursor.close()
        return userId
    }

    // ------------------- Category operations -------------------
    fun getAllCategories(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CATEGORIES, arrayOf(COL_CATEGORY_ID, COL_CATEGORY_NAME), null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(cursor.getInt(0) to cursor.getString(1))
        }
        cursor.close()
        return list
    }

    fun addCategory(name: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply { put(COL_CATEGORY_NAME, name) }
        return db.insert(TABLE_CATEGORIES, null, cv) != -1L
    }

    // ------------------- Expense operations -------------------
    fun addExpense(title: String, amount: Double, date: String, categoryId: Int, imagePath: String?): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_TITLE, title)
            put(COL_AMOUNT, amount)
            put(COL_DATE, date)
            put(COL_CATEGORY_ID_FK, categoryId)
            put(COL_IMAGE_PATH, imagePath)
        }
        return db.insert(TABLE_EXPENSES, null, cv) != -1L
    }

    fun getAllExpenses(): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            """
            SELECT e.*, c.$COL_CATEGORY_NAME 
            FROM $TABLE_EXPENSES e
            JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_ID_FK = c.$COL_CATEGORY_ID
            ORDER BY e.$COL_DATE DESC
            """, null
        )
    }

    fun getExpensesBetweenDates(startDate: String, endDate: String): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            """
            SELECT e.*, c.$COL_CATEGORY_NAME 
            FROM $TABLE_EXPENSES e
            JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_ID_FK = c.$COL_CATEGORY_ID
            WHERE e.$COL_DATE BETWEEN ? AND ?
            ORDER BY e.$COL_DATE DESC
            """, arrayOf(startDate, endDate)
        )
    }

    fun getCategoryTotals(startDate: String? = null, endDate: String? = null): Cursor {
        val db = readableDatabase
        val whereClause = if (startDate != null && endDate != null) {
            "WHERE e.$COL_DATE BETWEEN '$startDate' AND '$endDate'"
        } else ""
        return db.rawQuery(
            """
            SELECT c.$COL_CATEGORY_NAME, SUM(e.$COL_AMOUNT) as total
            FROM $TABLE_EXPENSES e
            JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_ID_FK = c.$COL_CATEGORY_ID
            $whereClause
            GROUP BY c.$COL_CATEGORY_NAME
            ORDER BY total DESC
            """, null
        )
    }

    fun getTotalExpensesForCurrentMonth(userId: Int, yearMonth: String): Double {
        // yearMonth format: YYYY-MM
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT SUM($COL_AMOUNT) FROM $TABLE_EXPENSES 
            WHERE $COL_DATE LIKE ?
            """, arrayOf("$yearMonth%")
        )
        var total = 0.0
        if (cursor.moveToFirst()) total = cursor.getDouble(0)
        cursor.close()
        return total
    }
}