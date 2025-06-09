package com.example.keepingtrack.repository

import androidx.lifecycle.LiveData
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.database.BookDao

class BookRepository(private val bookDao: BookDao) {
    val allBooks: LiveData<List<Book>> = bookDao.getAllBooks()

    suspend fun insert(book: Book) = bookDao.insert(book)
    suspend fun update(book: Book) = bookDao.update(book)
    suspend fun delete(bookId: Int) = bookDao.delete(bookId)
}