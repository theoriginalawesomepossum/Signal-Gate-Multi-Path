package com.signalgate.multipoint

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.signalgate.multipoint.ui.BlockedNumbersFragment
import com.signalgate.multipoint.ui.RecentCallsFragment
import com.signalgate.multipoint.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var statusTextView: TextView

    private lateinit var multiPointHubButton: MaterialButton
    private lateinit var helpButton: MaterialButton
    private lateinit var setDefaultButton: MaterialButton
    private lateinit var requestContactsButton: MaterialButton
    private lateinit var manageBlockedNumbersButton: MaterialButton

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var footerLogo: ImageView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener

    private var listenerRegistered = false

    private val setDefaultCallScreenerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {
            Toast.makeText(
                this,
                "Set as default call screener",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Failed to set as default call screener",
                Toast.LENGTH_SHORT
            ).show()
        }

        checkPermissionsAndRoles()
    }

    private val contactsPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            Toast.makeText(
                this,
                "Contacts permission granted",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Contacts permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }

        checkPermissionsAndRoles()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)

        initializeViews()

        setupBottomNavigation()

        setupButtons()

        updateAllColors()

        registerPreferenceListener()

        checkPermissionsAndRoles()

        // Default screen
        if (savedInstanceState == null) {
            showFragment(RecentCallsFragment())
        }
    }

    override fun onResume() {
        super.onResume()

        checkPermissionsAndRoles()

        updateAllColors()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (listenerRegistered) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(
                prefListener
            )

            listenerRegistered = false
        }
    }

    private fun initializeViews() {

        statusText = findViewById(R.id.statusText)

        statusTextView = findViewById(R.id.statusTextView)

        multiPointHubButton =
            findViewById(R.id.multiPointHubButton)

        helpButton =
            findViewById(R.id.helpButton)

        setDefaultButton =
            findViewById(R.id.setDefaultButton)

        requestContactsButton =
            findViewById(R.id.requestContactsButton)

        manageBlockedNumbersButton =
            findViewById(R.id.manageBlockedNumbersButton)

        bottomNavigation =
            findViewById(R.id.bottom_navigation)

        footerLogo =
            findViewById(R.id.footerLogo)
    }

    private fun setupBottomNavigation() {

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_blocked -> {
                    showFragment(BlockedNumbersFragment())
                }

                R.id.nav_recent -> {
                    showFragment(RecentCallsFragment())
                }

                R.id.nav_settings -> {
                    showFragment(SettingsFragment())
                }
            }

            true
        }
    }

    private fun setupButtons() {

        multiPointHubButton.setOnClickListener {

            Toast.makeText(
                this,
                "Multi-Point Hub coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }

        helpButton.setOnClickListener {

            Toast.makeText(
                this,
                "Help / Guide coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }

        setDefaultButton.setOnClickListener {
            requestSetDefaultCallScreener()
        }

        requestContactsButton.setOnClickListener {
            requestContactsPermission()
        }

        manageBlockedNumbersButton.setOnClickListener {
            showFragment(BlockedNumbersFragment())
        }
    }

    private fun registerPreferenceListener() {

        if (listenerRegistered) return

        prefListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->

                if (key?.startsWith("shield_") == true) {
                    updateAllColors()
                }
            }

        sharedPreferences
            .registerOnSharedPreferenceChangeListener(prefListener)

        listenerRegistered = true
    }

    fun reapplyTheme() {
        updateAllColors()
    }

    private fun updateAllColors() {

        updateBottomNavColor()

        updateGlobalButtonColors()

        updateFooterLogoTint()
    }

    private fun updateBottomNavColor() {

        val customColor = getThemeColor()

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(customColor, Color.WHITE)
        )

        bottomNavigation.itemIconTintList =
            colorStateList

        bottomNavigation.itemTextColor =
            colorStateList
    }

    private fun updateFooterLogoTint() {

        val customColor = getThemeColor()

        footerLogo.setColorFilter(customColor)
    }

    private fun getThemeColor(): Int {

        val red =
            sharedPreferences.getInt("shield_red", 66)

        val green =
            sharedPreferences.getInt("shield_green", 133)

        val blue =
            sharedPreferences.getInt("shield_blue", 244)

        return Color.rgb(red, green, blue)
    }

    /*
        PROFESSIONAL UNIVERSAL THEME SYSTEM

        Automatically themes:
        - ALL MaterialButtons
        - ALL future buttons
        - Fragment buttons
        - Dynamically added buttons

        No more manual button registration needed.
     */

    private fun updateGlobalButtonColors() {

        val customColor = getThemeColor()

        val root =
            findViewById<ViewGroup>(android.R.id.content)

        applyThemeToView(root, customColor)
    }

    private fun applyThemeToView(
        view: View,
        color: Int
    ) {

        when (view) {

            is MaterialButton -> {

                view.backgroundTintList =
                    ColorStateList.valueOf(color)

                view.setTextColor(Color.WHITE)
            }

            is ImageButton -> {

                view.setColorFilter(color)
            }
        }

        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                applyThemeToView(
                    view.getChildAt(i),
                    color
                )
            }
        }
    }

    private fun checkPermissionsAndRoles() {

        val isDefaultCallScreener =
            isDefaultCallScreener()

        val hasContactsPermission =
            hasReadContactsPermission()

        if (
            isDefaultCallScreener &&
            hasContactsPermission
        ) {

            statusText.text = "● Active"

            statusText.setTextColor(
                Color.parseColor("#00C853")
            )

            statusTextView.text =
                "SignalGate protection is fully active."

            setDefaultButton.visibility = View.GONE

            requestContactsButton.visibility = View.GONE

            manageBlockedNumbersButton.visibility =
                View.GONE

            bottomNavigation.visibility = View.VISIBLE

        } else {

            statusText.text = "● Setup Required"

            statusText.setTextColor(
                Color.parseColor("#F44336")
            )

            statusTextView.text =
                "Complete setup steps below to activate SignalGate."

            setDefaultButton.visibility =
                if (!isDefaultCallScreener) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            requestContactsButton.visibility =
                if (!hasContactsPermission) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            manageBlockedNumbersButton.visibility =
                View.VISIBLE

            bottomNavigation.visibility =
                View.GONE
        }
    }

    private fun showFragment(fragment: Fragment) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container,
                fragment
            )
            .commit()
    }

    private fun isDefaultCallScreener(): Boolean {

        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {

            val roleManager =
                getSystemService(RoleManager::class.java)

            roleManager.isRoleHeld(
                RoleManager.ROLE_CALL_SCREENING
            )

        } else {

            val telecomManager =
                getSystemService(
                    Context.TELECOM_SERVICE
                ) as TelecomManager

            packageName ==
                    telecomManager.defaultDialerPackage
        }
    }

    private fun requestSetDefaultCallScreener() {

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {

            val roleManager =
                getSystemService(RoleManager::class.java)

            val intent =
                roleManager.createRequestRoleIntent(
                    RoleManager.ROLE_CALL_SCREENING
                )

            setDefaultCallScreenerResult.launch(intent)

        } else {

            val intent =
                Intent(
                    TelecomManager.ACTION_CHANGE_DEFAULT_DIALER
                )

            intent.putExtra(
                TelecomManager
                    .EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            )

            setDefaultCallScreenerResult.launch(intent)
        }
    }

    private fun hasReadContactsPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {

        contactsPermissionResult.launch(
            android.Manifest.permission.READ_CONTACTS
        )
    }
}
