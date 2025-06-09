package com.example.keepingtrack.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.keepingtrack.R
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.repository.BookRepository
import com.example.keepingtrack.database.BookDatabase
import com.example.keepingtrack.databinding.ActivityBookDetailsBinding
import com.example.keepingtrack.viewmodel.BookViewModel
import com.example.keepingtrack.viewmodel.BookViewModelFactory
import java.io.File

class BookDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var viewModel: BookViewModel
    private lateinit var book: Book
    private var isEditMode = false
    private var imagePath: String? = null

    // Launcher for picking an image from the gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Copy the selected image to private storage
                    val inputStream = contentResolver.openInputStream(uri)
                    val file = File(filesDir, "book_image_${System.currentTimeMillis()}.jpg")
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    imagePath = file.absolutePath
                    binding.bookImagePreview.setImageURI(uri)
                    binding.bookImagePreview.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e("BookDetailsActivity", "Failed to load image from URI: $uri", e)
                }
            }
        } else {
            Log.e("BookDetailsActivity", "Image picker failed with result: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        val bookDatabase = BookDatabase.getDatabase(this)
        val bookRepository = BookRepository(bookDatabase.bookDao())
        viewModel = ViewModelProvider(this, BookViewModelFactory(bookRepository))
            .get(BookViewModel::class.java)

        // Retrieve the book from the intent
        book = intent.getParcelableExtra("EXTRA_BOOK", Book::class.java)
            ?: throw IllegalStateException("Book not passed to BookDetailsActivity")

        // Display the book's details initially
        binding.root.post {
            displayBookDetails()
        }

        // Set up the "Return" button to close the activity
        binding.returnButton.setOnClickListener {
            if (isEditMode) {
                !isEditMode
                displayBookDetails() // Return to display mode
            } else {
                finish()
            }
        }

        // Set up the "Pick Image" button for edit mode
        binding.pickImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        // Set up the "Edit/Save" button to toggle modes
        binding.editButton.setOnClickListener {
            if (isEditMode) {
                saveChanges()
            } else {
                enterEditMode()
            }
        }

        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete this book?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.delete(book.id)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Set up favorite book check
        updateFavoriteButton(book.isFavorite)
        binding.bookFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    // Display the book's details in TextViews
    private fun displayBookDetails() {
        binding.titleDisplay.text = book.title ?: "Not set"
        binding.authorDisplay.text = book.author ?: "Not set"
        binding.ratingDisplay.rating = book.rating ?: 0f
        binding.currentPageDisplay.text = book.currentPage?.toString() ?: "Not set"
        binding.totalPagesDisplay.text = book.totalPages?.toString() ?: "Not set"

        if (book.imagePath != null && File(book.imagePath).exists()) {
            binding.bookImagePreview.setImageURI(android.net.Uri.fromFile(File(book.imagePath)))
            binding.bookImagePreview.visibility = View.VISIBLE
        } else {
            binding.bookImagePreview.visibility = View.GONE
        }

        // Show display views, hide edit views
        binding.titleDisplay.visibility = View.VISIBLE
        binding.titleInput.visibility = View.GONE

        binding.authorLabel.visibility = View.VISIBLE
        binding.authorDisplay.visibility = View.VISIBLE
        binding.authorInput.visibility = View.GONE

        binding.ratingLabel.visibility = View.VISIBLE
        binding.ratingDisplay.visibility = View.VISIBLE
        binding.ratingInput.visibility = View.GONE

        binding.currentPageLabel.visibility = View.VISIBLE
        binding.currentPageDisplay.visibility = View.VISIBLE
        binding.currentPageInput.visibility = View.GONE

        binding.totalPagesLabel.visibility = View.VISIBLE
        binding.totalPagesDisplay.visibility = View.VISIBLE
        binding.totalPagesInput.visibility = View.GONE

        binding.pickImageButton.visibility = View.GONE
        binding.editButton.text = "Edit"
        isEditMode = false
    }

    // Switch to edit mode and populate EditText fields
    private fun enterEditMode() {
        binding.titleInput.setText(book.title ?: "")
        binding.authorInput.setText(book.author ?: "")
        binding.ratingInput.setText(book.rating?.toString() ?: "")
        binding.currentPageInput.setText(book.currentPage?.toString() ?: "")
        binding.totalPagesInput.setText(book.totalPages?.toString() ?: "")

        // Show edit views, hide display views
        binding.titleDisplay.visibility = View.GONE
        binding.titleInput.visibility = View.VISIBLE

        binding.authorLabel.visibility = View.GONE
        binding.authorDisplay.visibility = View.GONE
        binding.authorInput.visibility = View.VISIBLE

        binding.ratingLabel.visibility = View.GONE
        binding.ratingDisplay.visibility = View.GONE
        binding.ratingInput.visibility = View.VISIBLE

        binding.currentPageLabel.visibility = View.GONE
        binding.currentPageDisplay.visibility = View.GONE
        binding.currentPageInput.visibility = View.VISIBLE

        binding.totalPagesLabel.visibility = View.GONE
        binding.totalPagesDisplay.visibility = View.GONE
        binding.totalPagesInput.visibility = View.VISIBLE

        binding.pickImageButton.visibility = View.VISIBLE
        binding.editButton.text = "Save"
        isEditMode = true
    }

    private fun toggleFavorite() {
        // Toggle the favorite status
        val favoriteStatus = !book.isFavorite
        book = book.copy(isFavorite = favoriteStatus)

        // Update the button drawable
        updateFavoriteButton(favoriteStatus)

        // Update the book in the database
        viewModel.update(book)
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val drawableRes = if (isFavorite) R.drawable.ic_star_checked else R.drawable.ic_star_unchecked
        binding.bookFavorite.setImageResource(drawableRes)
    }

    // Save the edited changes and switch back to display mode
    private fun saveChanges() {
        try {
            book.apply {
                title = binding.titleInput.text.toString().ifEmpty { null }
                author = binding.authorInput.text.toString().ifEmpty { null }
                rating = binding.ratingInput.text.toString().toFloatOrNull()
                currentPage = binding.currentPageInput.text.toString().toIntOrNull()
                totalPages = binding.totalPagesInput.text.toString().toIntOrNull()
                imagePath = this@BookDetailsActivity.imagePath ?: book.imagePath // Use new image if selected
            }
            viewModel.update(book)
            displayBookDetails() // Return to display mode
        } catch (e: Exception) {
            Log.e("BookDetailsActivity", "Error updating book: ${e.message}")
        }
    }
}