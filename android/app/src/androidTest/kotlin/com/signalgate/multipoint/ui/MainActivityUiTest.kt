package com.signalgate.multipoint.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.signalgate.multipoint.MainActivity
import com.signalgate.multipoint.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `main activity launches and shows settings by default`() {
        onView(withId(R.id.previewShield)).check(matches(isDisplayed()))
        // You can add more checks once other fragments are fully working
    }

    @Test
    fun `bottom navigation is visible after permissions granted`() {
        // This test can be expanded as you fix other fragments
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
    }
}
