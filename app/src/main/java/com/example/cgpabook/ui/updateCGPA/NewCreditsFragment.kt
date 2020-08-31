package com.example.cgpabook.ui.updateCGPA

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.cgpabook.R
import com.example.cgpabook.utils.addButton
import com.example.cgpabook.utils.createllh
import com.example.cgpabook.utils.dashBoardButton

class NewCreditsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_new_credits, container, false)
        dashBoardButton(v)
        val llv = v.findViewById<LinearLayout>(R.id.llv_grade_button)
        v.findViewById<TextView>(R.id.txtcollege).text = "Select Subjects"
        v.findViewById<EditText>(R.id.college_choose).setOnClickListener {
            Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
        }
        Runnable {
            var llh: LinearLayout = createllh(llv)
            for (i in 1..9) {
                if (i % 3 == 1 && i != 1) {
                    llh = createllh(llv)
                }
                addButton(
                    llh,
                    i.toString(),
                    R.drawable.credits_button,
                    R.color.white
                ) { m -> buttononclick(m) }
            }
        }.run()
        return v

    }

    private fun buttononclick(v: View) {
        Toast.makeText(context, (v as Button).text, Toast.LENGTH_SHORT).show()
    }

}