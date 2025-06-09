package com.example.keepingtrack

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.keepingtrack.data.Book
import com.example.keepingtrack.ui.BookDetailsActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDetailsActivityTest {
    private lateinit var scenario: ActivityScenario<BookDetailsActivity>

    @Before
    fun setUp() {
        Intents.init()

        // Create a test book
        val book = Book(
            id = 1,
            title = "Test Book",
            author = "Test Author",
            rating = 4.5f,
            currentPage = 50,
            totalPages = 200,
            imagePath = null
        )

        // Create an Intent to launch BookDetailsActivity with the test book
        val intent = Intent().apply {
            setClassName("com.example.keepingtrack", "com.example.keepingtrack.ui.BookDetailsActivity")
            putExtra("EXTRA_BOOK", book)
        }

        // Launch the activity using the Intent
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        Intents.release()
        scenario.close()
    }
    @Test
    fun testToggleFavorite() {
        // Verify initial state (unchecked)
        onView(withId(R.id.book_favorite))
            .check(matches(withDrawable(R.drawable.ic_star_unchecked)))

        // Click the favorite button
        onView(withId(R.id.book_favorite)).perform(click())

        // Verify the drawable changes to checked
        onView(withId(R.id.book_favorite))
            .check(matches(withDrawable(R.drawable.ic_star_checked)))

        // Click again to toggle back
        onView(withId(R.id.book_favorite)).perform(click())

        // Verify the drawable changes back to unchecked
        onView(withId(R.id.book_favorite))
            .check(matches(withDrawable(R.drawable.ic_star_unchecked)))
    }
}

