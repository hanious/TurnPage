package com.example.keepingtrack

import android.view.View
import android.widget.ImageButton
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

fun withDrawable(resourceId: Int): BoundedMatcher<View, ImageButton> {
    return object : BoundedMatcher<View, ImageButton>(ImageButton::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with drawable resource $resourceId")
        }

        override fun matchesSafely(item: ImageButton): Boolean {
            val drawable = item.drawable
            val expectedDrawable = item.context.getDrawable(resourceId)
            return drawable != null && expectedDrawable != null &&
                    drawable.constantState == expectedDrawable.constantState
        }
    }
}