package com.signalgate.multipoint

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.signalgate.multipoint.ui.BlockedNumbersFragment
import com.signalgate.multipoint.ui.RecentCallsFragment
import com.signalgate.multipoint.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var multiPointHubButton: Button
    private lateinit var helpButton: Button
    private lateinit var setDefaultButton: Button
    private lateinit var requestContactsButton: Button
    private lateinit var manageBlockedNumbersButton: Button
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var sharedPreferences: SharedPreferences

    private val setDefaultCallScreenerResult =
        registerForActivityResult(
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

    private val contactsPermissionResult =
        registerForActivityResult(
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

        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    1001
                )
            }
        }

        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)

        statusText = findViewById(R.id.statusText)

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

        // Bottom navigation

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_blocked ->
                    showFragment(BlockedNumbersFragment())

                R.id.nav_recent ->
                    showFragment(RecentCallsFragment())

                R.id.nav_settings ->
                    showFragment(SettingsFragment())
            }

            true
        }

        // Button listeners

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

        updateBottomNavColor()
    }

    override fun onResume() {

        super.onResume()

        checkPermissionsAndRoles()

        updateBottomNavColor()
    }

    private fun updateBottomNavColor() {

        val red =
            sharedPreferences.getInt("shield_red", 66)

        val green =
            sharedPreferences.getInt("shield_green", 133)

        val blue =
            sharedPreferences.getInt("shield_blue", 244)

        val customColor =
            android.graphics.Color.rgb(
                red,
                green,
                blue
            )

        val colorStateList =
            android.content.res.ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    customColor,
                    0xFFFFFFFF.toInt()
                )
            )

        bottomNavigation.itemIconTintList =
            colorStateList

        bottomNavigation.itemTextColor =
            colorStateList
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

            statusText.text =
                "SignalGate Status: Active"

            statusText.setTextColor(
                android.graphics.Color.parseColor(
                    "#00C853"
                )
            )

            setDefaultButton.visibility =
                View.GONE

            requestContactsButton.visibility =
                View.GONE

            manageBlockedNumbersButton.visibility =
                View.GONE

            bottomNavigation.visibility =
                View.VISIBLE

        } else {

            statusText.text =
                "SignalGate Status: Not Active"

            statusText.setTextColor(
                android.graphics.Color.parseColor(
                    "#F44336"
                )
            )

            if (!isDefaultCallScreener) {

                setDefaultButton.visibility =
                    View.VISIBLE
            }

            if (!hasContactsPermission) {

                requestContactsButton.visibility =
                    View.VISIBLE
            }

            manageBlockedNumbersButton.visibility =
                View.VISIBLE

            bottomNavigation.visibility =
                View.GONE
        }
    }

    private fun showFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
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
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            )

            setDefaultCallScreenerResult.launch(intent)
        }
    }

    private fun hasReadContactsPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {

        contactsPermissionResult.launch(
            Manifest.permission.READ_CONTACTS
        )
    }
}
