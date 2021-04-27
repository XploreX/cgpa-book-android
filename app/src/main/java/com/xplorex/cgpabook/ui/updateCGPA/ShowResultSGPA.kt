package com.xplorex.cgpabook.ui.updateCGPA

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.xplorex.cgpabook.R
import com.xplorex.cgpabook.ui.SharedViewModel
import com.xplorex.cgpabook.utils.dashBoardButton
import com.xplorex.cgpabook.utils.getViewModel


class ShowResultSGPA : Fragment() {
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_show_result_sgpa, container, false)
        viewModel = getViewModel()
        dashBoardButton(v.findViewById(R.id.relativeLayoutParent))
        //initViewModelObservers()

        Handler().postDelayed({
            val drawable: Drawable = v.findViewById<ImageView>(R.id.tick).drawable
            if (drawable is AnimatedVectorDrawable)
                drawable.start()
            if (drawable is AnimatedVectorDrawableCompat)
                drawable.start()
        }, 500)
        v.findViewById<Button>(R.id.rate_button).setOnClickListener {
            context?.let { it1 -> openAppRating(it1) }
        }


        return v
    }

    private fun initViewModelObservers() {
        TODO("Not yet implemented")
    }

    fun openAppRating(context: Context) {
        // you can also use BuildConfig.APPLICATION_ID
        val appId: String = context.packageName
        val rateIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$appId")
        )
        var marketFound = false

        // find all applications able to handle our rateIntent
        val otherApps: List<ResolveInfo> = context.packageManager
            .queryIntentActivities(rateIntent, 0)
        for (otherApp in otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                == "com.android.vending"
            ) {
                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(
                    otherAppActivity.applicationInfo.packageName,
                    otherAppActivity.name
                )
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.component = componentName
                context.startActivity(rateIntent)
                marketFound = true
                break
            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appId")
            )
            context.startActivity(webIntent)
        }
    }
}