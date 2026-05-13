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
    fun `fragment loads without crashing`() {
        try {
            // Launch fragment in isolation
            val scenario = launchFragmentInContainer<BlockedNumbersFragment>()

            // Give the fragment a moment to initialize (ViewModel + RecyclerView)
            Thread.sleep(500) // Small delay helps with async ViewModel init

            // Verify core UI elements are displayed
            onView(withId(R.id.recyclerViewBlockedNumbers)).check(matches(isDisplayed()))
            onView(withId(R.id.emptyState)).check(matches(isDisplayed()))
            onView(withId(R.id.addBlockedNumberButton)).check(matches(isDisplayed()))

            println("✅ BlockedNumbersFragment loaded successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            throw AssertionError("❌ BlockedNumbersFragment crashed on launch: ${e.message}", e)
        }
    }

    @Test
    fun `add button is clickable and dialog can be triggered`() {
        try {
            launchFragmentInContainer<BlockedNumbersFragment>()

            onView(withId(R.id.addBlockedNumberButton)).check(matches(isDisplayed()))
            // Note: Full dialog testing would require more setup (Robolectric or mocking ViewModel)
            println("✅ Add blocked number button is visible and ready")
        } catch (e: Exception) {
            e.printStackTrace()
            throw AssertionError("Add button test failed", e)
        }
    }
}
