package com.example.cgpabook.ui.updateCGPA

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cgpabook.R
import com.example.cgpabook.classes.SubjectsData
import com.example.cgpabook.utils.dashBoardButton

class EnterMarksFragment : Fragment() {

    lateinit var llv: LinearLayout
    lateinit var grades: ArrayList<String>
    lateinit var llh: LinearLayout
    lateinit var data: ArrayList<SubjectsData>
    var index: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_enter_marks, container, false)
        dashBoardButton(v)
        grades = ArrayList(listOf("O", "A+", "A", "B+", "B", "C", "F"))
        llv = v.findViewById(R.id.llv_grade_button)
        for (i in 0 until grades.size) {
            if (i % 4 == 0) {
                llh = createllh(llv)
            }
            addButton(llh, grades[i])
        }
//        addButton(llh,"2")
//        addButton(llh,"3")
        return v
    }

    fun createllh(llv: ViewGroup): LinearLayout {
        val llh = LinearLayout(context)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llh.orientation = LinearLayout.HORIZONTAL
        llv.addView(llh, params)
        return llh
    }

    fun addButton(llh: ViewGroup, text: String) {
        val v = LayoutInflater.from(context).inflate(R.layout.grade_buttons, llh, false) as Button
        v.text = text
        v.setOnClickListener {
            fetchdata()
        }
        llh.addView(v, v.layoutParams)
    }

    fun fetchdata() {
        if (index == data.size) {
            return
        }
        view!!.findViewById<TextView>(R.id.txt_subjectsleft).text =
            (data.size - 1 - index).toString()
        view!!.findViewById<TextView>(R.id.txt_subname).text = data[index].subName
        view!!.findViewById<TextView>(R.id.txt_credits).text = data[index].credits.toString()
    }
}