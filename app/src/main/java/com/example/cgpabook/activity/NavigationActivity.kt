package com.example.cgpabook.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.cgpabook.R
import com.example.cgpabook.ui.SharedViewModel
import com.example.cgpabook.ui.profile.ProfileFragment
import com.example.cgpabook.ui.updateCGPA.CollegeChoose
import com.example.cgpabook.utils.HelperStrings
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView

class NavigationActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var lastchecked: Int = R.id.nav_profile
    private lateinit var navView: NavigationView
    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        // on Create Init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        // Initialize View Model
        viewModel = ViewModelProviders.of(
            this,
            SavedStateViewModelFactory(application, this)
        )[SharedViewModel::class.java]

        // Add all the received data from Main Activity Intent(name,email,avatar)
        if (intent != null) {
            viewModel.setVal(HelperStrings.name, intent.getStringExtra(HelperStrings.name))
            viewModel.setVal(HelperStrings.email, intent.getStringExtra(HelperStrings.email))
            viewModel.setVal(HelperStrings.photoUrl, intent.getStringExtra(HelperStrings.photoUrl))
        }

        // Initialize the variable
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Setup the Fragment Manager Transactions

        // Starting Page is Profile Page
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.nav_host_fragment,
                ProfileFragment()
            ).addToBackStack(getString(R.string.menu_profile))
            .commit()
        navView.setCheckedItem(R.id.nav_profile)

        // Item Listener
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_sign_out -> {
                    val intent = Intent(this, MainActivity::class.java)
                    val gso =
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build()
                    val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
                    mGoogleSignInClient.signOut().addOnCompleteListener {
                        startActivity(intent)
                        finish()
                    }
                }
                R.id.nav_feedback -> {
                    Toast.makeText(this@NavigationActivity, "Feedback", Toast.LENGTH_SHORT).show()

                    // Don't Change the Last Checked Item
                    navView.setCheckedItem(lastchecked)

                    // Debug: println("lastchecked:$lastchecked,${it.itemId},${navView.checkedItem?.itemId}")
                }
                R.id.nav_share -> {
                    Toast.makeText(this@NavigationActivity, "Share", Toast.LENGTH_SHORT).show()

                    // Don't Change the Last Checked Item
                    navView.setCheckedItem(lastchecked)

                    // Debug: println("lastchecked:$lastchecked,${it.itemId},${navView.checkedItem?.itemId}")
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

            // Save the Data in View Model after every Fragment Change
            viewModel.writeToDisk()
            return@setNavigationItemSelectedListener true
        }
        // End Fragment Transaction Setup


        // View Model Observers
        viewModel.getElement<String>(HelperStrings.name).observe(this, Observer {
            navView.getHeaderView(0).findViewById<TextView>(R.id.txt_name).text = it
        })
        viewModel.getElement<String>(HelperStrings.email).observe(this, Observer {
            navView.getHeaderView(0).findViewById<TextView>(R.id.txt_email).text = it
        })
        viewModel.getElement<Double>(HelperStrings.cgpa).observe(this, Observer {
            navView.getHeaderView(0).findViewById<TextView>(R.id.txt_cgpa).text =
                "CGPA: ${String.format("%.2f", it)}"
        })
        viewModel.getElement<String>(HelperStrings.photoUrl).observe(this, Observer {
            if (it != null)
                Glide.with(navView).load(it).circleCrop()
                    .into(navView.getHeaderView(0).findViewById(R.id.imgprofile))
            else
                Glide.with(this).load(getDrawable(R.drawable.profilepic))
                    .circleCrop().into(navView.getHeaderView(0).findViewById(R.id.imgprofile))
        })
        // If college present, update the email field with the college name
        viewModel.getElement<String>(HelperStrings.college).observe(this, Observer {
            findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
                .findViewById<TextView>(R.id.txt_email).text = it
        })
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            viewModel.writeToDisk()
            when (supportFragmentManager.backStackEntryCount) {
                2 -> {
                    // Go to Profile from every other Fragment on back press
                    supportFragmentManager.popBackStack()
                    navView.setCheckedItem(R.id.nav_profile)
                }
                1 -> {
                    // If on Profile Page, Exit the app
                    finish()
                }
                else -> super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        // Write to Disk when Exiting the app
        viewModel.writeToDisk()
        super.onDestroy()
    }


}