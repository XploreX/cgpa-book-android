package com.xplorex.cgpabook.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Cache
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.xplorex.cgpabook.BuildConfig
import com.xplorex.cgpabook.R
import com.xplorex.cgpabook.ui.SharedViewModel
import com.xplorex.cgpabook.utils.*
import org.json.JSONObject


class ProfileFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private var volleyQueue: MySingleton? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Fragment Init
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        viewModel = getViewModel()

        // Add the top right DashBoard Button
        dashBoardButton(v.findViewById(R.id.relativeLayoutParent))

        // Setup the Display Variables
        val viewModelObserverStrings = ArrayList<String>(
            listOf(
                HelperStrings.name,
                HelperStrings.cgpa,
                HelperStrings.college,
                HelperStrings.photoUrl
            )
        )

        // Init Variables
        val ll = v.findViewById<LinearLayout>(R.id.sem_ll)
        val pullToRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.pulltorefresh)

        v.findViewById<TextView>(R.id.updateProfile).setOnClickListener {
            goToUpdateProfile()
        }

        val profileUrl = HelperStrings.url + "/user/gpa-data"
        val userGpaDataRequest = object :
            JsonObjectRequest(Method.GET, profileUrl, null, Response.Listener {

                println("recvObj: $it")
                val dataToReceive = ArrayList<String>(
                    listOf(
                        HelperStrings.college,
                        HelperStrings.branch,
                        HelperStrings.course,
                        HelperStrings.semdata,
                        HelperStrings.unlocked,
                        HelperStrings.rated
                    )
                )
                for (i in dataToReceive) {
                    if (it.has(i)) {
                        viewModel.setVal(i, it.get(i))
                    }
                }
                // Initialize the unlocked Fields variable
                initUnlocked()
                pullToRefreshLayout.isRefreshing = false
                viewModel.writeToDisk()

            }, Response.ErrorListener {
                println("recvObjError")
                initUnlocked()
                pullToRefreshLayout.isRefreshing = false
                errorHandler(it)
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] =
                    "Bearer ${viewModel.getVal<String>(HelperStrings.tokenId)}"
                return params
            }

            override fun setCacheEntry(entry: Cache.Entry?): Request<*>? {
                return null
            }
        }

        volleyQueue = context?.let { MySingleton.getInstance(it) }
        if (getSyncState(requireContext(), viewModel)) {

            // set Refreshing true when loading data
            pullToRefreshLayout.isRefreshing = true
            viewModel.setVal(HelperStrings.updateProfile, false)
            volleyQueue?.addToRequestQueue(userGpaDataRequest)
        } else {
            // Initialize the unlocked Fields variable
            initUnlocked()
        }

        viewModel.getElement<Boolean>(HelperStrings.updateProfile)
            .observe(viewLifecycleOwner, Observer {
                if (it == true) {
                    v.findViewById<TextView>(R.id.updateProfile).text = "Update Profile"
                    context?.let {
                        AlertDialog.Builder(it)
                            .setTitle("Confirm")
                            .setMessage("Your Profile page isn't updated, update profile?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes) { _, _ ->
                                goToUpdateProfile()
                            }
                            .setNegativeButton(android.R.string.no) { _, _ ->
                            }
                            .show()
                        viewModel.setVal(
                            HelperStrings.infobar,
                            "Your Profile page isn't updated, Please update your profile"
                        )
                    }
                } else {
                    v.findViewById<TextView>(R.id.updateProfile).text = "Edit Profile"
                    viewModel.setVal(
                        HelperStrings.infobar,
                        "No Semester Data Exists, Please update Your CGPA"
                    )
                }
            })

        // Pull to refresh
        pullToRefreshLayout.setOnRefreshListener {
            if (getSyncState(requireContext(), viewModel)) {
                volleyQueue?.let { volleyQueue ->
                    volleyQueue.addToRequestQueue(userGpaDataRequest)
                }
            } else {
                context?.let {
                    AlertDialog.Builder(it)
                        .setTitle("Confirm")
                        .setMessage("You have some changes which aren't backup up, it will be lost, do you really want to refresh?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            volleyQueue?.addToRequestQueue(userGpaDataRequest)
                        }
                        .setNegativeButton(android.R.string.no) { _, _ ->
                            pullToRefreshLayout.isRefreshing = false
                        }
                        .show()
                }
            }
        }

        // This is to fix Issue #1
        viewModel.setVal(HelperStrings.cgpa, 0.0)

        for (value in viewModelObserverStrings) {
            when (value) {
                HelperStrings.photoUrl -> {
                    viewModel.getElement<String>(value).observe(viewLifecycleOwner, Observer {
                        if (it != null)
                            Glide.with(this).load(it).circleCrop()
                                .into(v.findViewById(R.id.imgprofile))
                        else
                            Glide.with(this)
                                .load(resources.getDrawable(R.drawable.profilepic, null))
                                .circleCrop().into(v.findViewById(R.id.imgprofile))
                    })
                }
                HelperStrings.name -> {
                    viewModel.getElement<String>(value).observe(viewLifecycleOwner, Observer {
                        v.findViewById<TextView>(R.id.profile_name).text = it
                    })
                }
                HelperStrings.college -> {
                    viewModel.getElement<String>(value).observe(viewLifecycleOwner, Observer {
                        v.findViewById<TextView>(R.id.profile_college).text = it
                    })
                }
                HelperStrings.cgpa -> {
                    viewModel.getElement<Double?>(value).observe(viewLifecycleOwner, Observer {
                        if (it == 0.0 || it == null)
                            v.findViewById<TextView>(R.id.profile_cgpa).text = "CGPA: None"
                        else
                            v.findViewById<TextView>(R.id.profile_cgpa).text =
                                "CGPA: ${String.format("%.2f", it.toDouble())}"
                    })
                }
                else -> {
                    throw Error("Not Implemented")
                }
            }
        }
        if (viewModel.getVal<JSONObject>(HelperStrings.semdata) == null) {
            viewModel.setVal(HelperStrings.semdata, null)
        }

        // Show any info when changed
        viewModel.getElement<String>(HelperStrings.infobar).observe(viewLifecycleOwner, Observer {
            // make sure that if data exists, no overwrite is being done by the info bar
            if (viewModel.getVal<JSONObject>(HelperStrings.semdata) == null) {
                ll.removeAllViews()
                val v1 = LayoutInflater.from(context)
                    .inflate(R.layout.no_semdata, ll as ViewGroup, false)
                val textView = v1.findViewById<TextView>(R.id.infobar)
                textView.text = it
                ll.addView(v1)
            }
        })

        viewModel.getElement<JSONObject>(HelperStrings.semdata)
            .observe(viewLifecycleOwner, Observer { allSemData ->
                // Clear the Linear Layout
                ll.removeAllViews()

                // Debug: println(allSemData)
                var cgpa: Double = 0.0

                if (allSemData == null) {
                    val v1 = LayoutInflater.from(context)
                        .inflate(R.layout.no_semdata, ll as ViewGroup, false)
                    val textView = v1.findViewById<TextView>(R.id.infobar)
                    textView.text = viewModel.getVal(HelperStrings.infobar)
                    ll.addView(v1)
                }
                // Get all the Semester Nos
                allSemData?.let {
                    val keys = ArrayList(allSemData.keys().asSequence().toList())
                    keys.sort()
                    for (currentKey in keys) {

                        //Inflate a View
                        val v1 = LayoutInflater.from(context)
                            .inflate(R.layout.profile_semdata, ll as ViewGroup, false)

                        // Get the semData from the allSemData
                        val semData: JSONObject = JSONObject(allSemData.getString(currentKey))

                        // Setup the Data from semData to view
                        v1.findViewById<TextView>(R.id.semno).text = "Semester No: $currentKey"
                        v1.findViewById<TextView>(R.id.sgpa).text =
                            "SGPA: ${String.format(
                                "%.2f",
                                semData.get(HelperStrings.sgpa).toString().toDouble()
                            )}"
                        v1.findViewById<ImageView>(R.id.share_sgpa).setOnClickListener {
                            val sendIntent = Intent()
                            sendIntent.action = Intent.ACTION_SEND
                            sendIntent.putExtra(
                                Intent.EXTRA_TEXT,
                                "Hey, My GPA for Semester $currentKey is ${String.format(
                                    "%.2f",
                                    semData.get(HelperStrings.sgpa).toString().toDouble()
                                )} . Wanna calculate yours? Download CGPABook from https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID} and effortlessly calculate your CGPA"
                            )
                            sendIntent.type = "text/plain"
                            startActivity(sendIntent)
                        }
                        v1.findViewById<ImageView>(R.id.btn_delete).setOnClickListener {
                            AlertDialog.Builder(context)
                                .setTitle("Confirm")
                                .setMessage("Do you really want to delete semester $currentKey result?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes) { _, _ ->
                                    // set sync state false so that profile fragment doesn't download data before uploading the new data
                                    context?.let { it1 -> setSyncState(it1, false, viewModel) }
                                    allSemData.remove(currentKey)
                                    if (allSemData.length() != 0)
                                        viewModel.setVal(HelperStrings.semdata, allSemData)
                                    else
                                        viewModel.setVal(HelperStrings.semdata, null)
                                }
                                .setNegativeButton(android.R.string.no, null).show()
                        }

                        // Add the SGPAs for final CGPA
                        cgpa += (semData.get(HelperStrings.sgpa).toString().toDouble())

                        // Add the view to Linear Layout
                        ll.addView(v1)
                    }

                    // Divide the Added SGPAs by the total no of semesters
                    cgpa /= allSemData.length()

                }
                // Update the new CGPA
                viewModel.setVal(HelperStrings.cgpa, cgpa)
                viewModel.writeToDisk()
            })

        return v
    }

    private fun initUnlocked() {
        if (viewModel.getVal<Int>(HelperStrings.unlocked) == null) viewModel.setVal(
            HelperStrings.unlocked,
            0
        )
    }


    override fun onDestroy() {
        volleyQueue?.let {
            it.getRequestQueue()?.cancelAll { true }
        }
        super.onDestroy()
    }

}