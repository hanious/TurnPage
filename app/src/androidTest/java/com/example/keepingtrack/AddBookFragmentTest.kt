package com.example.keepingtrack

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.keepingtrack.ui.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddBookFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        // Navigate to AddBookFragment
        onView(withId(R.id.fab_add_book)).perform(click())
    }

    @Test
    fun testValidationError() {
        // Leave fields empty and click save
        onView(withId(R.id.save_book_button)).perform(click())

        // Check for validation errors
        onView(withId(R.id.title_input)).check(matches(hasErrorText("Title is required")))
        onView(withId(R.id.author_input)).check(matches(hasErrorText("Author is required")))
        onView(withId(R.id.rating_input)).check(matches(hasErrorText("Rating is required")))
    }
}