package com.signalgate.multipoint

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.signalgate.multipoint.ui.BlockedNumbersFragment

class MainActivity : AppCompatActivity() {

    private val REQUEST_ID_SET_DEFAULT_CALL_SCREENER = 1
    private val REQUEST_CODE_READ_CONTACTS = 2

    private lateinit var statusTextView: TextView
    private lateinit var setDefaultButton: Button
    private lateinit var requestContactsButton: Button
    private lateinit var manageBlockedNumbersButton: Button
    private lateinit var fragmentContainer: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        statusTextView = TextView(this).apply {
            text = "Checking app status..."
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        rootLayout.addView(statusTextView)

        setDefaultButton = Button(this).apply {
            text = "Set as Default Call Screener"
            setOnClickListener { requestSetDefaultCallScreener() }
            visibility = View.GONE
        }
        rootLayout.addView(setDefaultButton)

        requestContactsButton = Button(this).apply {
            text = "Grant Contacts Permission"
            setOnClickListener { requestContactsPermission() }
            visibility = View.GONE
        }
        rootLayout.addView(requestContactsButton)

        manageBlockedNumbersButton = Button(this).apply {
            text = "Manage Blocked Numbers"
            setOnClickListener { showBlockedNumbersFragment() }
            visibility = View.GONE
        }
        rootLayout.addView(manageBlockedNumbersButton)

        fragmentContainer = FragmentContainerView(this).apply {
            id = View.generateViewId() // Generate a unique ID for the container
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        rootLayout.addView(fragmentContainer)

        setContentView(rootLayout)
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
            manageBlockedNumbersButton.visibility = View.VISIBLE
            showBlockedNumbersFragment() // Automatically show the management UI
        } else {
            statusTextView.text = "Setup required:"
            if (!isDefaultCallScreener) {
                setDefaultButton.visibility = View.VISIBLE
            } else {
                setDefaultButton.visibility = View.GONE
            }
            if (!hasContactsPermission) {
                requestContactsButton.visibility = View.VISIBLE
            } else {
                requestContactsButton.visibility = View.GONE
            }
            manageBlockedNumbersButton.visibility = View.GONE
            // Clear fragment container if permissions are not met
            supportFragmentManager.findFragmentById(fragmentContainer.id)?.let {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
        }
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
            startActivityForResult(intent, REQUEST_ID_SET_DEFAULT_CALL_SCREENER)
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivityForResult(intent, REQUEST_ID_SET_DEFAULT_CALL_SCREENER)
        }
    }

    private fun hasReadContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Contacts permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
            checkPermissionsAndRoles() // Re-check status after permission result
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID_SET_DEFAULT_CALL_SCREENER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Set as default call screener", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to set as default call screener", Toast.LENGTH_SHORT).show()
            }
            checkPermissionsAndRoles() // Re-check status after role result
        }
    }

    private fun showBlockedNumbersFragment() {
        supportFragmentManager.beginTransaction()
            .replace(fragmentContainer.id, BlockedNumbersFragment())
            .commit()
    }
}
