package com.example.keepingtrack.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.keepingtrack.R
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.database.BookDatabase
import com.example.keepingtrack.databinding.FragmentBooksBinding
import com.example.keepingtrack.repository.BookRepository
import com.example.keepingtrack.ui.adapter.BookAdapter
import com.example.keepingtrack.viewmodel.BookViewModel
import com.example.keepingtrack.viewmodel.BookViewModelFactory

class BooksFragment : Fragment() {
    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookViewModel
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksBinding.inflate(inflater, container, false)
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

        // Pass viewModel to BookAdapter
        adapter = BookAdapter(viewModel, onBookClicked)
        binding.bookList.layoutManager = LinearLayoutManager(requireContext())
        binding.bookList.adapter = adapter

        // Bind ViewModel to layout
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner // Enables LiveData updates

        // Observe the book list and update the RecyclerView
        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            adapter.submitList(books)
        }

        // Set up FAB to navigate to AddBookFragment
        binding.fabAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_booksFragment_to_addBookFragment)
        }

        // Observe navigation from AddBookFragment
        viewModel.navigateToBooks.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate == true) {
                viewModel.onNavigatedToBooks()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}