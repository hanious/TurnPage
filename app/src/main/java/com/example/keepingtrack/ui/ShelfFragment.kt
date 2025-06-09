package com.example.keepingtrack.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.database.BookDatabase
import com.example.keepingtrack.databinding.FragmentShelfBinding
import com.example.keepingtrack.repository.BookRepository
import com.example.keepingtrack.ui.adapter.BookAdapter
import com.example.keepingtrack.viewmodel.BookViewModel
import com.example.keepingtrack.viewmodel.BookViewModelFactory

class ShelfFragment : Fragment() {

    private var _binding: FragmentShelfBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookViewModel
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Define the onBookClicked function to handle book clicks
        val onBookClicked: (Book) -> Unit = { book ->
            val intent = Intent(requireContext(), BookDetailsActivity::class.java).apply {
                putExtra("EXTRA_BOOK", book)
            }
            startActivity(intent)
        }

        // Initialize ViewModel
        val bookDatabase = BookDatabase.getDatabase(requireContext())
        val bookRepository = BookRepository(bookDatabase.bookDao())
        viewModel = ViewModelProvider(this, BookViewModelFactory(bookRepository))
            .get(BookViewModel::class.java)

        // Set up RecyclerView with the adapter
        adapter = BookAdapter(viewModel, onBookClicked)
        binding.favoriteBookList.adapter = adapter

        // Observe the book list and filter for favorites
        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            val favoriteBooks = books.filter { it.isFavorite }
            adapter.submitList(favoriteBooks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}