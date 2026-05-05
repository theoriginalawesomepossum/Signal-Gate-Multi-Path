package com.signalgate.multipoint

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.signalgate.multipoint.ui.BlockedNumbersFragment
import com.signalgate.multipoint.ui.RecentCallsFragment
import com.signalgate.multipoint.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    private val REQUEST_ID_SET_DEFAULT_CALL_SCREENER = 1
    private val REQUEST_CODE_READ_CONTACTS = 2

    private lateinit var statusTextView: TextView
    private lateinit var setDefaultButton: Button
    private lateinit var requestContactsButton: Button
    private lateinit var manageBlockedNumbersButton: Button
    private lateinit var bottomNavigation: BottomNavigationView

    private val setDefaultCallScreenerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Set as default call screener", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to set as default call screener", Toast.LENGTH_SHORT).show()
        }
        checkPermissionsAndRoles()
    }

    private val contactsPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Contacts permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show()
        }
        checkPermissionsAndRoles()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)          // we'll add this ID in XML if needed
        setDefaultButton = findViewById(R.id.setDefaultButton)
        requestContactsButton = findViewById(R.id.requestContactsButton)
        manageBlockedNumbersButton = findViewById(R.id.manageBlockedNumbersButton)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_blocked -> showFragment(BlockedNumbersFragment())
                R.id.nav_recent -> showFragment(RecentCallsFragment())
                R.id.nav_settings -> showFragment(SettingsFragment())
            }
            true
        }

        setDefaultButton.setOnClickListener { requestSetDefaultCallScreener() }
        requestContactsButton.setOnClickListener { requestContactsPermission() }
        manageBlockedNumbersButton.setOnClickListener { showFragment(BlockedNumbersFragment()) }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsAndRoles()
    }

    private fun checkPermissionsAndRoles() {
        val isDefaultCallScreener = isDefaultCallScreener()
        val hasContactsPermission = hasReadContactsPermission()

        if (isDefaultCallScreener && hasContactsPermission) {
            statusTextView.text = "Signal Gate is active and ready!"
            setDefaultButton.visibility = View.GONE
            requestContactsButton.visibility = View.GONE
            manageBlockedNumbersButton.visibility = View.GONE
            bottomNavigation.visibility = View.VISIBLE

            if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
                showFragment(BlockedNumbersFragment())
            }
        } else {
            statusTextView.text = "Setup required:"
            if (!isDefaultCallScreener) setDefaultButton.visibility = View.VISIBLE
            if (!hasContactsPermission) requestContactsButton.visibility = View.VISIBLE
            manageBlockedNumbersButton.visibility = View.VISIBLE
            bottomNavigation.visibility = View.GONE
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun isDefaultCallScreener(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            return packageName == telecomManager.defaultDialerPackage
        }
    }

    private fun requestSetDefaultCallScreener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            setDefaultCallScreenerResult.launch(intent)
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
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
        contactsPermissionResult.launch(android.Manifest.permission.READ_CONTACTS)
    }
}
