package com.example.keepingtrack.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "books")
data class Book(
    @PrimaryKey (autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "title")
    var title: String?,

    @ColumnInfo(name = "author")
    var author: String?,

    @ColumnInfo(name = "rating")
    var rating: Float?,

    @ColumnInfo(name = "current_page")
    var currentPage: Int? = 0,

    @ColumnInfo(name = "total_pages")
    var totalPages: Int?,

    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,

    @ColumnInfo(name = "image_path")
    var imagePath: String? // Local file path

): Parcelable