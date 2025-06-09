package com.example.keepingtrack.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.keepingtrack.R
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.databinding.ItemBookBinding
import com.example.keepingtrack.viewmodel.BookViewModel

class BookAdapter(
    private val viewModel: BookViewModel,
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    class BookViewHolder(
        private val binding: ItemBookBinding,
        private val viewModel: BookViewModel,
        private val onBookClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.book = book
            val drawableRes =
                if (book.isFavorite) R.drawable.ic_star_checked else R.drawable.ic_star_unchecked
            binding.bookFavorite.setImageResource(drawableRes)

            binding.bookFavorite.setOnClickListener {
                val newFavoriteStatus = !book.isFavorite
                val updatedBook = book.copy(isFavorite = newFavoriteStatus)
                viewModel.update(updatedBook)
            }

            binding.root.setOnClickListener {
                onBookClick(book)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding, viewModel, onBookClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book)
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}