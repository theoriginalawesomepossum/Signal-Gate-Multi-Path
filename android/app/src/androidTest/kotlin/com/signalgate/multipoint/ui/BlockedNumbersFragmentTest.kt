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
        try {
            // This will catch crashes during fragment creation
            val scenario = launchFragmentInContainer<BlockedNumbersFragment>()

            // Verify main views are displayed
            onView(withId(R.id.recyclerViewBlockedNumbers)).check(matches(isDisplayed()))
            onView(withId(R.id.emptyState)).check(matches(isDisplayed()))
            onView(withId(R.id.addBlockedNumberButton)).check(matches(isDisplayed()))

            println("✅ BlockedNumbersFragment loaded successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            throw AssertionError("BlockedNumbersFragment crashed on launch: ${e.message}", e)
        }
    }
}
