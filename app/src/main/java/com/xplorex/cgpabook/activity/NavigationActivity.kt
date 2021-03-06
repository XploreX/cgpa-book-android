package com.xplorex.cgpabook.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.xplorex.cgpabook.BuildConfig
import com.xplorex.cgpabook.R
import com.xplorex.cgpabook.receiver.ConnectivityBroadcastReceiver
import com.xplorex.cgpabook.ui.SharedViewModel
import com.xplorex.cgpabook.ui.profile.ProfileFragment
import com.xplorex.cgpabook.ui.updateCGPA.SemesterChooseFragment
import com.xplorex.cgpabook.utils.HelperStrings
import com.xplorex.cgpabook.utils.MySingleton
import com.xplorex.cgpabook.utils.getSyncState
import com.xplorex.cgpabook.utils.setSyncState

class NavigationActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var lastchecked = R.id.nav_profile
    private lateinit var navView: NavigationView
    private lateinit var viewModel: SharedViewModel
    private val broadcastReceiver = ConnectivityBroadcastReceiver()
    private var broadcastEnabled = false


    override fun onCreate(savedInstanceState: Bundle?) {

        // on Create Init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        // Initialize View Model
        viewModel = ViewModelProviders.of(
            this,
            SavedStateViewModelFactory(application, this)
        ).get(HelperStrings.NavigationActivity, SharedViewModel::class.java)
        // Add all the received data from Main Activity Intent(name,email,avatar)
        if (intent != null) {
            viewModel.setVal(HelperStrings.name, intent.getStringExtra(HelperStrings.name))
            viewModel.setVal(HelperStrings.email, intent.getStringExtra(HelperStrings.email))
            viewModel.setVal(HelperStrings.photoUrl, intent.getStringExtra(HelperStrings.photoUrl))
            viewModel.setVal(HelperStrings.tokenId, intent.getStringExtra(HelperStrings.tokenId))
            viewModel.writeToDisk()
        }

        // Setup Broadcast Listener
        viewModel.getElement<Boolean>(HelperStrings.synced).observe(this, Observer { synced ->
            if (synced == false) {
                disableBroadcast()
                enableBroadcast()
            } else
                disableBroadcast()
        })

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
            navView.setCheckedItem(it.itemId)
            when (it.itemId) {
                R.id.nav_sign_out -> {
                    val sync = getSyncState(this, viewModel)
                    if (!sync) {
                        AlertDialog.Builder(this)
                            .setTitle("Confirm")
                            .setMessage("Your Data isn't backup up yet, if you logout it will be lost, do you really want to log out?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes) { _, _ ->
                                signOut()
                            }
                            .setNegativeButton(R.string.no) { _, _ ->
                                navView.setCheckedItem(
                                    lastchecked
                                )
                            }
                            .show()

                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("Confirm")
                            .setMessage("Are you sure you want to sign out?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes) { _, _ ->
                                signOut()
                            }
                            .setNegativeButton(R.string.no) { _, _ ->
                                navView.setCheckedItem(
                                    lastchecked
                                )
                            }
                            .show()
                    }

                }
                R.id.nav_feedback -> {
                    val uri =
                        Uri.parse("mailto:${getString(R.string.support_email)}?subject=Feedback&body=Please enter your feedback here")
                    val sendIntent = Intent(Intent.ACTION_SENDTO, uri)
                    if (sendIntent.resolveActivity(packageManager) != null) {
                        startActivity(Intent.createChooser(sendIntent, "Send Feedback"))
                    }

                    // Don't Change the Last Checked Item
                    navView.setCheckedItem(lastchecked)
                }
                R.id.nav_share -> {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "Hey, My CGPA is ${String.format(
                            "%.2f",
                            viewModel.getVal<Double>(HelperStrings.cgpa)
                        )} . Wanna calculate yours? Download CGPABook from https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID} and effortlessly calculate your CGPA"
                    )
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)

                    // Don't Change the Last Checked Item
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
                            SemesterChooseFragment()
                        ).addToBackStack(getString(R.string.menu_select_college))
                        .commit()
                    lastchecked = it.itemId
                }
            }
            drawerLayout.closeDrawers()
            // Debug: println("itemid:${it.itemId},lastchecked:$lastchecked, navViewchecked:${navView.checkedItem}")

            // by returning false, we can handle the selected item ourselves (Fixes #7)
            return@setNavigationItemSelectedListener false
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
            val view = navView.getHeaderView(0).findViewById<TextView>(R.id.txt_cgpa)
            if (it == 0.0 || it == null)
                view.text = "CGPA: None"
            else
                view.text = "CGPA: ${String.format("%.2f", it.toDouble())}"
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
        // unlocked observer
        viewModel.getElement<Int>(HelperStrings.unlocked).observe(this, Observer {
            println("unlocked :$it")
            if (it < 3)
                viewModel.setVal(HelperStrings.updateProfile, true)
            else
                viewModel.setVal(HelperStrings.updateProfile, false)
        })
    }

    private fun signOut() {
        reset()
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

    // delete all the user stored data and flush caches
    private fun reset() {
        setSyncState(this, true, viewModel)
        viewModel.deleteViewModel()
        viewModelStore.clear()
        savedStateRegistry.unregisterSavedStateProvider(HelperStrings.NavigationActivity)
        MySingleton.getInstance(this)
            ?.getRequestQueue()?.cache?.remove("${HelperStrings.url}/user/gpa-data")
    }

    // disable broadcast when exiting the app
    private fun disableBroadcast() {
        if (broadcastEnabled) {
            try {
                applicationContext.unregisterReceiver(broadcastReceiver)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            broadcastEnabled = false
        }
    }

    // if any update is done, enable broadcast to send the updated data as soon as internet is available
    private fun enableBroadcast() {
        if (!broadcastEnabled) {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            }
            applicationContext.registerReceiver(broadcastReceiver, filter)
            broadcastEnabled = true
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
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

}