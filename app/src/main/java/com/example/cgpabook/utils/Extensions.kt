package com.example.cgpabook.utils

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProviders
import com.android.volley.VolleyError
import com.example.cgpabook.R
import com.example.cgpabook.ui.SharedViewModel
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun AppCompatActivity.hideSystemUI() {
    // Enables regular immersive mode.
    // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
    // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Hide the nav bar and status bar
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
}

// Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
fun AppCompatActivity.showSystemUI() {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
}

fun Fragment.dashBoardButton(v: View) {
    val layoutInflater = LayoutInflater.from(context)
    val c = layoutInflater.inflate(R.layout.dashboard_button, v as ViewGroup, false)
    c.setOnClickListener {
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.START)
    }
    v.addView(c)
}

fun Fragment.progressBarInit(v: View): ProgressBar {
    activity!!.window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
    val progressBar =
        ProgressBar(context)
    val params = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
    )
    params.addRule(RelativeLayout.CENTER_IN_PARENT)
    val relativeLayout = RelativeLayout(context)
    relativeLayout.addView(progressBar, params)
    val frameparams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    (v as ViewGroup).addView(relativeLayout, frameparams)
    progressBar.visibility = View.VISIBLE
    val progressOverlay = v.findViewById<FrameLayout>(R.id.progress_overlay)
    progressOverlay.visibility = View.VISIBLE
    return progressBar
}

fun Fragment.progressBarDestroy(v: View, p: ProgressBar) {
    val progressOverlay = v.findViewById<FrameLayout>(R.id.progress_overlay)
    activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    progressOverlay.visibility = View.GONE
    p.visibility = View.GONE
}

fun Fragment.createllh(llv: ViewGroup): LinearLayout {
    val llh = LinearLayout(context)
    val params = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    llh.orientation = LinearLayout.HORIZONTAL
    llv.addView(llh, params)
    return llh
}

fun Fragment.addButton(
    llh: ViewGroup,
    text: String,
    button: Int,
    color: Int,
    bar: (m: View) -> Unit
) {
    val v = LayoutInflater.from(context).inflate(R.layout.grade_buttons, llh, false) as Button
    v.text = text
    v.setTextColor(resources.getColor(color, null))
    v.setOnClickListener {
        bar(v)
    }
    llh.addView(v, v.layoutParams)
    v.background = resources.getDrawable(button, null)
}

fun addparams(url: String, body: JSONObject): String {
    var newurl = url
    var first = true
    for (key in body.keys()) {
        val value: String = body.getString(key)
        if (!first) {
            newurl += "&"
        }
        if (value.trim() != "")
            newurl += Uri.encode(key) + '=' + Uri.encode(value)
        first = false
    }
    return newurl
}

fun Fragment.errorhandler(it: VolleyError): Boolean {
    if (it == null) {
        Toast.makeText(context, "Couldn't connect", Toast.LENGTH_SHORT).show()
        return true
    }
    if (it.networkResponse == null)
        return false
    val ob = JSONObject(String(it.networkResponse.data))
    Toast.makeText(context, ob.getString("message"), Toast.LENGTH_SHORT).show()
    return true
}

fun Fragment.errorhandler(ob: JSONObject): Boolean {
    if (!ob.has("message"))
        return false
    Toast.makeText(context, ob.getString("message"), Toast.LENGTH_SHORT).show()
    return true
}

fun Fragment.goToProfile() {
    activity!!.supportFragmentManager.popBackStack(resources.getString(R.string.menu_profile), 0)
    activity!!.findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.nav_profile)
}

fun Fragment.getViewModel() =
    (activity?.run {
        ViewModelProviders.of(
            this,
            SavedStateViewModelFactory(application, this)
        )[SharedViewModel::class.java]
    }
        ?: throw Exception("Invalid Activity"))

