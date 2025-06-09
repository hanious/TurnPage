package com.example.keepingtrack.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.keepingtrack.R
import com.example.keepingtrack.repository.BookRepository
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.database.BookDatabase
import com.example.keepingtrack.databinding.FragmentAddBookBinding
import com.example.keepingtrack.viewmodel.BookViewModel
import com.example.keepingtrack.viewmodel.BookViewModelFactory
import kotlin.text.isBlank
import kotlin.text.toIntOrNull

class AddBookFragment : Fragment() {
    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookViewModel
    private var book = Book(
        id = 0,
        title = null,
        author = null,
        rating = null,
        currentPage = null,
        totalPages = null,
        imagePath = null
    )

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = getPathFromUri(uri)
                if (path != null) {
                    // Update the book with the new image path while preserving other fields
                    book = book.copy(
                        imagePath = path,
                        title = binding.titleInput.text.toString(),
                        author = binding.authorInput.text.toString(),
                        rating = binding.ratingInput.text.toString().toFloatOrNull() ?: 0f,
                        currentPage = binding.currentPageInput.text.toString().toIntOrNull() ?: 0,
                        totalPages = binding.totalPagesInput.text.toString().toIntOrNull() ?: 0
                    )
                    // Update the image preview
                    updateImagePreview(path)
                } else {
                    Log.e("AddBookFragment", "Failed to get image path from URI")
                }
            }
        } else {
            Log.e("AddBookFragment", "Image picker failed with result: ${result.resultCode}")
        }
    }

    private fun updateImagePreview(imagePath: String) {
        binding.bookImagePreview.setImageURI(android.net.Uri.parse(imagePath))
        // Make image view visible if it wasn't already
        binding.bookImagePreview.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val bookDatabase = BookDatabase.getDatabase(requireContext())
        val bookRepository = BookRepository(bookDatabase.bookDao())
        viewModel = ViewModelProvider(this, BookViewModelFactory(bookRepository))
            .get(BookViewModel::class.java)

        // Bind ViewModel and Book to the layout
        binding.viewModel = viewModel
        binding.book = book
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up the "Pick Image" button
        binding.pickImageButton.setOnClickListener {
            openImagePicker() // Directly call the image picker
            book.imagePath?.let { updateImagePreview(it) }
        }

        binding.saveBookButton.setOnClickListener {
            if (validateForm()) {
                try {
                    val newBook = Book(
                        id = book.id,
                        title = binding.titleInput.text.toString(),
                        author = binding.authorInput.text.toString(),
                        rating = binding.ratingInput.text.toString().toFloatOrNull() ?: 0f,
                        currentPage = binding.currentPageInput.text.toString().toIntOrNull() ?: 0,
                        totalPages = binding.totalPagesInput.text.toString().toIntOrNull() ?: 0,
                        imagePath = book.imagePath
                    )
                    // Update the book reference
                    book = newBook
                    // Insert the book into the database
                    viewModel.insert(book)
                    // Directly navigate after insert
                    findNavController().navigate(R.id.action_addBookFragment_to_booksFragment)
                } catch (e: Exception) {
                    Log.e("AddBookFragment", "Error saving book: ${e.message}")
                }
            }
        }

        binding.returnButton.setOnClickListener {
            // Navigate back to the previous fragment
            findNavController().popBackStack()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun getPathFromUri(uri: android.net.Uri): String? {
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate title
        if (binding.titleInput.text.toString().isBlank()) {
            binding.titleInput.error = "Title is required"
            isValid = false
        } else {
            binding.titleInput.error = null
        }

        // Validate author
        if (binding.authorInput.text.toString().isBlank()) {
            binding.authorInput.error = "Author is required"
            isValid = false
        } else {
            binding.authorInput.error = null
        }

        // Validate rating
        val rating = binding.ratingInput.text.toString().toFloatOrNull() ?: -1f
        if (binding.ratingInput.text.toString().isBlank()) {
            binding.ratingInput.error = "Rating is required"
            isValid = false
        } else {
            if (rating < 0 || rating > 5) {
                binding.ratingInput.error = "Rating must be between 0 and 5"
                isValid = false
            } else {
                binding.ratingInput.error = null
            }
        }

        val currentPage = binding.currentPageInput.text.toString().toIntOrNull() ?: -1
        val totalPages = binding.totalPagesInput.text.toString().toIntOrNull() ?: -1
        // Validate current page
        if (binding.currentPageInput.text.toString().isBlank()) {
            binding.currentPageInput.error = "Current page is required"
            isValid = false
        } else {
            if (totalPages <= 0) {
                binding.ratingInput.error = "Total pages must be greater than 0"
                isValid = false
            } else {
                binding.ratingInput.error = null
            }
        }

        // Validate total pages
        if (binding.totalPagesInput.text.toString().isBlank()) {
            binding.totalPagesInput.error = "Total pages is required"
            isValid = false
        }

        // Compare current page and total pages
        if (currentPage >= 0 && totalPages > 0 && currentPage > totalPages) {
            binding.currentPageInput.error = "Current page cannot exceed total pages"
            isValid = false
        }

        // Validate image path
        if (book.imagePath.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}