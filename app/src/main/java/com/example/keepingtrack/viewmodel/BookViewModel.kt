package com.example.keepingtrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    val allBooks: LiveData<List<Book>> = repository.allBooks

    // Navigation trigger for adding a book
    private val _navigateToBooks = MutableLiveData<Boolean>()
    val navigateToBooks: LiveData<Boolean> get() = _navigateToBooks

    // Result of insert operation
    private val _insertResult = MutableLiveData<Result<Unit>>()

    private val _updateResult = MutableLiveData<Result<Unit>>()

    fun insert(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.insert(book)
            _insertResult.postValue(Result.success(Unit))
            _navigateToBooks.postValue(true)
        } catch (e: Exception) {
            _insertResult.postValue(Result.failure(e))
        }
    }

    fun update(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.update(book)
            _updateResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _updateResult.postValue(Result.failure(e))
        }
    }

    fun delete(bookId: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(bookId)
    }

    // Reset navigation triggers
    fun onNavigatedToBooks() {
        _navigateToBooks.value = false
    }
}