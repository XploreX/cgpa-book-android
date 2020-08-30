package com.example.cgpabook.activity

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.cgpabook.R
import com.example.cgpabook.ui.Profile.ProfileFragment
import com.example.cgpabook.ui.updateCGPA.CollegeChoose
import com.example.cgpabook.ui.updateCGPA.NewCreditsFragment
import com.google.android.material.navigation.NavigationView

class NavigationActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        val navView: NavigationView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.nav_host_fragment,
                NewCreditsFragment()
            )//.addToBackStack("Home")
            .commit()
        navView.setCheckedItem(R.id.nav_profile)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_feedback -> {
                    Toast.makeText(this@NavigationActivity, "Feedback", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_share -> {
                    Toast.makeText(this@NavigationActivity, "Share", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            ProfileFragment()
                        ).addToBackStack(getString(R.string.menu_profile))
                        .commit()
                }
                R.id.nav_update_cgpa -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            CollegeChoose()
                        ).addToBackStack(getString(R.string.menu_select_college))
                        .commit()
                }
            }
            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else
            super.onBackPressed()
    }


}