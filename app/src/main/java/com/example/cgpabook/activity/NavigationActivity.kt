package com.example.cgpabook.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.example.cgpabook.R
import com.example.cgpabook.ui.Profile.ProfileFragment
import com.example.cgpabook.ui.updateCGPA.CollegeChoose
import com.google.android.material.navigation.NavigationView

class NavigationActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var lastchecked: Int = R.id.nav_profile
    private lateinit var navView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        drawerLayout = findViewById(R.id.drawer_layout)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.nav_host_fragment,
                ProfileFragment()
            ).addToBackStack(getString(R.string.menu_profile))
            .commit()
        navView = findViewById(R.id.nav_view)
        navView.setCheckedItem(R.id.nav_profile)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_feedback -> {
                    Toast.makeText(this@NavigationActivity, "Feedback", Toast.LENGTH_SHORT).show()
                    navView.setCheckedItem(lastchecked)
                }
                R.id.nav_share -> {
                    Toast.makeText(this@NavigationActivity, "Share", Toast.LENGTH_SHORT).show()
                    navView.setCheckedItem(lastchecked)
                }
                R.id.nav_profile -> {
                    supportFragmentManager.popBackStack(
                        getString(R.string.menu_profile),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            ProfileFragment()
                        ).addToBackStack(getString(R.string.menu_profile))
                        .commit()
                    lastchecked = it.itemId
                }
                R.id.nav_update_cgpa -> {
                    supportFragmentManager.popBackStack(
                        getString(R.string.menu_select_college),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            CollegeChoose()
                        ).addToBackStack(getString(R.string.menu_select_college))
                        .commit()
                    lastchecked = it.itemId
                }
            }
            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            when (supportFragmentManager.backStackEntryCount) {
                2 -> {
                    supportFragmentManager.popBackStack()
                    navView.setCheckedItem(R.id.nav_profile)
                }
                1 -> {
                    finish()
                }
                else -> super.onBackPressed()
            }
        }
    }


}