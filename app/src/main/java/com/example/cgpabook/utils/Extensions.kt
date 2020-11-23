package com.example.cgpabook.utils

import android.content.Context
import android.net.ConnectivityManager
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
import com.example.cgpabook.ui.profile.ProfileEditFragment
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.io.*


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

// Injects the hamburger icon and makes it clickable in fragment with root layout v
fun Fragment.dashBoardButton(v: View) {
    val layoutInflater = LayoutInflater.from(context)
    val c = layoutInflater.inflate(R.layout.dashboard_button, v as ViewGroup, false)
    c.setOnClickListener {
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.START)
    }
    v.addView(c)
}

// Initializes a progress Bar and returns a ProgressBar object to be used for progressBarDestroy
fun Fragment.progressBarInit(v: View): ProgressBar {

    // Disable taps when progress bar is being shown
    activity?.let {
        it.window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    // init progress bar
    val progressBar = ProgressBar(context)
    val params = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
    )
    params.addRule(RelativeLayout.CENTER_IN_PARENT)
    val relativeLayout = RelativeLayout(context)
    relativeLayout.addView(progressBar, params)
    val layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    (v as ViewGroup).addView(relativeLayout, layoutParams)
    progressBar.visibility = View.VISIBLE

    // overlay.xml layout should be included in the file where you need to show progressbar
    val progressOverlay = v.findViewById<FrameLayout>(R.id.progress_overlay)
    progressOverlay.visibility = View.VISIBLE
    return progressBar
}

// Destroys the progress bar and restores functionality
fun Fragment.progressBarDestroy(v: View, p: ProgressBar) {
    val progressOverlay = v.findViewById<FrameLayout>(R.id.progress_overlay)
    // Restores taps
    activity?.let {
        it.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    progressOverlay.visibility = View.GONE
    p.visibility = View.GONE
}

// creates a horizontal linear layout and adds it to vertical linear layout given in the parameter
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

// adds the grade_button to linear layout
// TODO: can be made more generic by providing layout to be used in params also
fun Fragment.addButton(
    llh: ViewGroup, text: String, button: Int, color: Int, bar: (m: View) -> Unit
) {
    val v = LayoutInflater.from(context).inflate(R.layout.grade_buttons, llh, false) as Button
    v.text = text
    v.setTextColor(resources.getColor(color, null))
    v.setOnClickListener { bar(v) }
    llh.addView(v, v.layoutParams)
    v.background = resources.getDrawable(button, null)
}

// adds params to get request of a url("?" should be present in the url in the end)
fun addparams(url: String, body: JSONObject): String {
    var newurl = url
    var first = true
    for (key in body.keys()) {
        val value: String = body.getString(key)
        if (!first) {
            newurl += "&"
        }
        if (value.trim() != "") newurl += Uri.encode(key) + '=' + Uri.encode(value)
        first = false
    }
    return newurl
}

// a generic error handler for the VolleyErrors
fun Fragment.errorHandler(it: VolleyError): Boolean {

    // If No network response received
    if (it.networkResponse == null) {
        it.printStackTrace()

        // check network connection
        if (!isNetworkConnected()) {
            showToast("Couldn't connect, No Internet", Toast.LENGTH_SHORT)
        } else showToast("Couldn't connect", Toast.LENGTH_SHORT)
    } else {
        // if some other status code received, show the message string
        val ob = JSONObject(String(it.networkResponse.data))

        if (!ob.has("error")) {
            showToast("Unknown Error occurred", Toast.LENGTH_SHORT); return false
        } else showToast(ob.getJSONObject("error").getString("message"), Toast.LENGTH_SHORT)
    }
    return true

}

// a context safe show Toast call for fragments
fun Fragment.showToast(message: CharSequence, duration: Int) {
    if (context != null) {
        Toast.makeText(context, message, duration).show()
    }
}

// a extension to go to profile page
fun Fragment.goToProfile() {
    activity?.let {
        it.supportFragmentManager.popBackStack(resources.getString(R.string.menu_profile), 0)
        it.findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.nav_profile)
    }
}

// returns true if network is available
fun Fragment.isNetworkConnected(): Boolean {
    val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val x = cm?.activeNetworkInfo?.isConnected
    return cm?.activeNetwork != null && x!!
}

fun isNetworkConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val x = cm?.activeNetworkInfo?.isConnected
    return cm?.activeNetwork != null && x!!
}

// returns SharedViewModel object for fragments
fun Fragment.getViewModel() =
    (activity?.run {
        ViewModelProviders.of(
            this,
            SavedStateViewModelFactory(application, this)
        ).get(HelperStrings.NavigationActivity, SharedViewModel::class.java)
    } ?: throw Exception("Invalid Activity"))

// read an object from disk in string format with the given filename
// being used for reading jsonobject from disk
fun readFromDisk(filename: String): String? {
    val file = File(filename)
    if (file.exists()) {
        return try {
            val fis: FileInputStream = FileInputStream(file)
            val isr = InputStreamReader(fis)
            val bufferedReader = BufferedReader(isr)
            val sb = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            fis.close()
            sb.toString()
        } catch (fileNotFound: FileNotFoundException) {
            null
        } catch (ioException: IOException) {
            null
        }
    }
    return null
}

// write a string to disk in the given filename
// being used for writing jsonobject to disk
fun writeToDisk(filename: String, string: String?): Boolean {
    return try {
        val fos: FileOutputStream =
            FileOutputStream(File(filename), false)
        if (string != null) {
            fos.write(string.toByteArray())
        }
        fos.close()
        true
    } catch (fileNotFound: FileNotFoundException) {
        false
    } catch (ioException: IOException) {
        false
    }
}

fun getSyncState(context: Context, viewModel: SharedViewModel?): Boolean {
    val sharedProviders = context.applicationContext.getSharedPreferences(
        HelperStrings.sharedPrefs,
        Context.MODE_PRIVATE
    )
    viewModel?.setVal(HelperStrings.synced, sharedProviders.getBoolean(HelperStrings.synced, true))
    return sharedProviders.getBoolean(HelperStrings.synced, true)
}

fun setSyncState(context: Context, boolean: Boolean, viewModel: SharedViewModel?) {
    val sharedProviders = context.applicationContext.getSharedPreferences(
        HelperStrings.sharedPrefs,
        Context.MODE_PRIVATE
    )
    sharedProviders.edit().putBoolean(HelperStrings.synced, boolean).apply()
    viewModel?.setVal(HelperStrings.synced, boolean)
}

fun Fragment.goToUpdateProfile() {
    requireActivity().supportFragmentManager.beginTransaction()
        .replace(R.id.nav_host_fragment, ProfileEditFragment())
        .addToBackStack(getString(R.string.menu_update_profile)).commit()
}

