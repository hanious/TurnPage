package com.example.keepingtrack.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.keepingtrack.data.Book

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): LiveData<List<Book>>

    @Insert
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun delete(bookId: Int)
}