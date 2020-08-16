package com.example.cgpabook.activity

import android.os.Bundle
import android.view.Menu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.cgpabook.CollegeChoose
import com.example.cgpabook.R
import com.example.cgpabook.ui.home.HomeFragment
import com.example.cgpabook.ui.slideshow.SlideshowFragment
import com.google.android.material.navigation.NavigationView

class NavigationActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        findViewById<RelativeLayout>(R.id.btn_dashboard).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        val navView: NavigationView = findViewById(R.id.nav_view)
//        val navController = findNavController(R.id.nav_host_fragment)
        drawerLayout = findViewById(R.id.drawer_layout)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
//            ), drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, HomeFragment())//.addToBackStack("Home")
            .commit()
        navView.setCheckedItem(R.id.nav_home)
        navView.setNavigationItemSelectedListener {
            val id = it.itemId
            when (id) {
                R.id.nav_feedback -> {
                    Toast.makeText(this@NavigationActivity, "Feedback", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_share -> {
                    Toast.makeText(this@NavigationActivity, "Share", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment()).addToBackStack("Home")
                        .commit()
                }
                R.id.nav_gallery -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, CollegeChoose()).addToBackStack("Gallery")
                        .commit()
                }
                R.id.nav_slideshow -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, SlideshowFragment())
                        .addToBackStack("Slideshow")
                        .commit()
                }
            }
            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navigation, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}