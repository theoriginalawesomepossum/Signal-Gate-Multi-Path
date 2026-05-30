package com.signalgate.multipoint.ui

import com.signalgate.multipoint.MainActivity
import com.signalgate.multipoint.fragments.SettingsFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.signalgate.multipoint.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFragmentUiTest {

    @Test
    fun settingsFragment_launches_without_crash() {

        launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_MaterialComponents
        )
    }
}
