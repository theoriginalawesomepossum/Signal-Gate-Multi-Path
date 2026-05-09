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
    fun mainActivity_launches_without_crashing() {

        onView(withId(R.id.headerContainer))
            .check(matches(isDisplayed()))

        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
    }
}
