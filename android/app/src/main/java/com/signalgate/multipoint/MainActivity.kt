package com.signalgate.multipoint

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import com.signalgate.multipoint.fragments.DashboardFragment
import com.signalgate.multipoint.fragments.SourcesFragment
import com.signalgate.multipoint.fragments.CallLogFragment
import com.signalgate.multipoint.fragments.BlockListFragment
import com.signalgate.multipoint.fragments.SettingsFragment
import com.signalgate.multipoint.fragments.AuditLogFragment
import com.signalgate.multipoint.fragments.AboutFragment

/**
 * MainActivity is the main entry point of the SignalGate Multi-Port application.
 * It hosts the navigation drawer and manages fragment transitions between different sections.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        // Set up toolbar
        setSupportActionBar(toolbar)

        // Set up navigation drawer toggle
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up navigation view listener
        navigationView.setNavigationItemSelectedListener(this)

        // Load the default fragment (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment(), "Dashboard")
            navigationView.setCheckedItem(R.id.nav_dashboard)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment? = when (item.itemId) {
            R.id.nav_dashboard -> DashboardFragment()
            R.id.nav_sources -> SourcesFragment()
            R.id.nav_call_log -> CallLogFragment()
            R.id.nav_block_list -> BlockListFragment()
            R.id.nav_settings -> SettingsFragment()
            R.id.nav_audit_log -> AuditLogFragment()
            R.id.nav_about -> AboutFragment()
            else -> null
        }

        if (fragment != null) {
            loadFragment(fragment, item.title.toString())
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Loads a fragment into the main container.
     */
    private fun loadFragment(fragment: Fragment, title: String) {
        supportActionBar?.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
