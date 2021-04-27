package com.xplorex.cgpabook.ui.updateCGPA

import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.xplorex.cgpabook.R
import com.xplorex.cgpabook.ui.SharedViewModel
import com.xplorex.cgpabook.utils.*


class ShowResultSGPA(private val sgpa: Double?) : Fragment() {
    private lateinit var viewModel: SharedViewModel
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_show_result_sgpa, container, false)
        viewModel = getViewModel()
        dashBoardButton(v.findViewById(R.id.relativeLayoutParent))
        textView = v.findViewById(R.id.sgpatext)
        initViewModelObservers()

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

        v.findViewById<ImageView>(R.id.prev).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        v.findViewById<ImageView>(R.id.next).setOnClickListener {
            goToProfile()
        }


        return v
    }

    private fun initViewModelObservers() {
        viewModel.getElement<Double>(HelperStrings.cgpa).observe(viewLifecycleOwner,
            Observer {
                textView.text = "Your CGPA is ${
                    String.format(
                        "%.2f",
                        it.toDouble()
                    )
                } with an SGPA of ${String.format("%.2f", sgpa)}"
            }
        )
    }


}