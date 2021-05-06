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
import android.widget.Toast
import androidx.core.content.ContextCompat
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
        initViewModelObservers(v)

        Handler().postDelayed({
            val drawable: Drawable = v.findViewById<ImageView>(R.id.tick).drawable
            if (drawable is AnimatedVectorDrawable)
                drawable.start()
            if (drawable is AnimatedVectorDrawableCompat)
                drawable.start()
        }, 500)

        val rateButton = v.findViewById<Button>(R.id.rate_button)
        rateButton.setOnClickListener {
            context?.let { it1 -> openAppRating(it1) }
            viewModel.setVal(HelperStrings.rated, true)
        }

        v.findViewById<ImageView>(R.id.next).setOnClickListener {
            goToProfile()
        }

        if (viewModel.getVal<Boolean>(HelperStrings.rated) == null)
            viewModel.setVal(HelperStrings.rated, false)

        return v
    }

    private fun initViewModelObservers(v: View) {
        viewModel.getElement<Double>(HelperStrings.cgpa).observe(viewLifecycleOwner,
            Observer {
                var str = ""
                if (sgpa!! >= 8)
                    str += "Congratulations! "
                else if (sgpa >= 7 && sgpa < 8)
                    str += "Great! "
                else if (sgpa >= 6 && sgpa < 7)
                    str += "You can do better. "
                else if (sgpa < 6)
                    str += "Please try harder. "
                str += "Your SGPA is ${
                    String.format(
                        "%.2f",
                        sgpa
                    )
                } making your CGPA ${String.format("%.2f", it.toDouble())}"
                textView.text = str
            }
        )
        viewModel.getElement<Boolean>(HelperStrings.rated)
            .observe(viewLifecycleOwner, Observer { rated ->
                if (rated) {
                    val rateButton = v.findViewById<Button>(R.id.rate_button)
                    context?.let { context ->
                        rateButton.background =
                            ContextCompat.getDrawable(context, R.drawable.grade_buttons)
                        rateButton.text = "Thank you for rating our app!"
                        rateButton.setTextColor(ContextCompat.getColor(context, R.color.black))
                        setSyncState(context, false, viewModel)
                    }
                } else {
                    showToast(
                        "If you like our app, please consider rating it. Thank you!",
                        Toast.LENGTH_LONG
                    )
                }
            })
    }


}