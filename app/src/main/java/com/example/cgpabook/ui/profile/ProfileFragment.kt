package com.example.cgpabook.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.cgpabook.R
import com.example.cgpabook.ui.SharedViewModel
import com.example.cgpabook.utils.HelperStrings
import com.example.cgpabook.utils.dashBoardButton
import com.example.cgpabook.utils.getViewModel

class ProfileFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        viewModel = getViewModel()
        val arraylist = ArrayList<String>(
            listOf(
                HelperStrings.name,
                HelperStrings.cgpa,
                HelperStrings.college,
                HelperStrings.photourl
            )
        )
        for (value in arraylist) {
            when (value) {
                HelperStrings.photourl -> {
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
                    v.findViewById<TextView>(R.id.profile_cgpa).text = "CGPA: None"
                    viewModel.getElement<Double>(value).observe(viewLifecycleOwner, Observer {
                        v.findViewById<TextView>(R.id.profile_cgpa).text =
                            "CGPA: ${String.format("%.2f", it)}"
                    })
                }
                else -> {
                    throw Error("Not Implemented")
                }
            }
        }

        viewModel.getElement<String>(HelperStrings.name).observe(viewLifecycleOwner, Observer {
        })
        dashBoardButton(v)

        return v
    }

}