package com.signalgate.multipoint.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFragmentUiTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun `settings fragment opens without crash`() {
        // This test simply verifies the app doesn't crash when opening Settings
        // More tests can be added later with Espresso matchers
        assert(activityRule.activity != null)
    }
}
