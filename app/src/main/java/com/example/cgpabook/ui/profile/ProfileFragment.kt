package com.example.cgpabook.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.cgpabook.R
import com.example.cgpabook.ui.SharedViewModel
import com.example.cgpabook.utils.HelperStrings
import com.example.cgpabook.utils.dashBoardButton
import com.example.cgpabook.utils.getViewModel
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Fragment Init
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        viewModel = getViewModel()

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
                    viewModel.getElement<Double>(value).observe(viewLifecycleOwner, Observer {
                        if (it == 0.0)
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

        // Add the top right DashBoard Button
        dashBoardButton(v)

        viewModel.getElement<JSONObject>(HelperStrings.semdata)
            .observe(viewLifecycleOwner, Observer { allSemData ->
                // Clear the Linear Layout
                ll.removeAllViews()

                // Debug: println(allSemData)
                var cgpa: Double = 0.0

                // Get all the Semester Nos
                allSemData?.let {
                    allSemData
                    for (currentKey in allSemData.keys()) {

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
                        v1.findViewById<ImageView>(R.id.btn_delete).setOnClickListener {
                            allSemData.remove(currentKey)
                            if (allSemData.length() != 0)
                                viewModel.setVal(HelperStrings.semdata, allSemData)
                            else
                                viewModel.setVal(HelperStrings.semdata, null)
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
            })



        return v
    }

}