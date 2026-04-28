package com.budgetbuddy.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.budgetbuddy.R
import com.budgetbuddy.data.DBHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var spinnerCategory: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var tvDate: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnCapturePhoto: Button
    private lateinit var btnPickFromGallery: Button
    private lateinit var ivPreview: ImageView
    private lateinit var btnSave: Button

    private var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private var imagePath: String? = null
    private var currentPhotoPath: String? = null
    private var categoryList: List<Pair<Int, String>> = emptyList()
    private var selectedCategoryId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        dbHelper = DBHelper(this)

        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        tvDate = findViewById(R.id.tvDate)
        btnPickDate = findViewById(R.id.btnPickDate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto)
        btnPickFromGallery = findViewById(R.id.btnPickFromGallery)
        ivPreview = findViewById(R.id.ivPreview)
        btnSave = findViewById(R.id.btnSaveExpense)

        loadCategories()

        tvDate.text = selectedDate
        btnPickDate.setOnClickListener { showDatePicker() }
        btnCapturePhoto.setOnClickListener { dispatchTakePictureIntent() }
        btnPickFromGallery.setOnClickListener { openGallery() }
        btnSave.setOnClickListener { saveExpense() }
    }

    private fun loadCategories() {
        categoryList = dbHelper.getAllCategories()
        val categoryNames = categoryList.map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        if (categoryList.isNotEmpty()) selectedCategoryId = categoryList[0].first

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                selectedCategoryId = categoryList[pos].first
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _: DatePicker, year: Int, month: Int, day: Int ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            tvDate.text = selectedDate
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile = createImageFile()
            currentPhotoPath = photoFile.absolutePath
            val photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    imagePath = currentPhotoPath
                    ivPreview.setImageURI(Uri.fromFile(File(imagePath)))
                    ivPreview.visibility = ImageView.VISIBLE
                }
            }
            REQUEST_PICK_IMAGE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        imagePath = getRealPathFromURI(selectedImageUri)
                        ivPreview.setImageURI(selectedImageUri)
                        ivPreview.visibility = ImageView.VISIBLE
                    }
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, proj, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun saveExpense() {
        val title = etTitle.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()
        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Title and amount required", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbHelper.addExpense(title, amount, selectedDate, selectedCategoryId, imagePath)
        if (success) {
            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PICK_IMAGE = 2
    }
}