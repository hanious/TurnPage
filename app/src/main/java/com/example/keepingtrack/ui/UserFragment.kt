package com.example.keepingtrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keepingtrack.database.BookDatabase
import com.example.keepingtrack.databinding.FragmentUserBinding
import com.example.keepingtrack.repository.BookRepository
import com.example.keepingtrack.viewmodel.BookViewModel
import com.example.keepingtrack.viewmodel.BookViewModelFactory

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val bookDatabase = BookDatabase.getDatabase(requireContext())
        val bookRepository = BookRepository(bookDatabase.bookDao())
        viewModel = ViewModelProvider(this, BookViewModelFactory(bookRepository))
            .get(BookViewModel::class.java)

        // Observe the book list and calculate statistics
        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            // Total books added
            val totalBooks = books.size
            binding.booksAdded.text = "Books Added: $totalBooks"

            // Books finished
            val finishedBooks = books.count { it.currentPage != null && it.totalPages != null && it.currentPage == it.totalPages }
            binding.booksFinished.text = "Books Finished Reading: $finishedBooks"

            // Books in progress
            val inProgressBooks = books.count { book ->
                val current = book.currentPage
                val total = book.totalPages
                if (current != null && total != null) {
                    current > 0 && current < total
                } else {
                    false
                }
            }
            binding.booksInProgress.text = "Books in Progress: $inProgressBooks"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}