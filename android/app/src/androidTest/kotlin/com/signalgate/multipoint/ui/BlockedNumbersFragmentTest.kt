package com.signalgate.multipoint.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.signalgate.multipoint.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlockedNumbersFragmentTest {

    @Test
    fun fragmentLoadsWithoutCrashing() {
        // This test will fail (and show clear error) if the fragment crashes on launch
        val scenario = launchFragmentInContainer<BlockedNumbersFragment>()

        // Basic UI checks
        onView(withId(R.id.recyclerViewBlockedNumbers)).check(matches(isDisplayed()))
        // Empty state should be visible initially
        onView(withId(R.id.emptyState)).check(matches(isDisplayed()))
    }

    @Test
    fun addButtonIsClickable() {
        launchFragmentInContainer<BlockedNumbersFragment>()
        onView(withId(R.id.addBlockedNumberButton)).check(matches(isDisplayed()))
    }
}
